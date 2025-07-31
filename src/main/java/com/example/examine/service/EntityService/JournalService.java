package com.example.examine.service.EntityService;

import com.example.examine.dto.request.TextSimilarityRequest;
import com.example.examine.dto.response.Crawler.JournalExtract;
import com.example.examine.dto.response.JSEResponse;
import com.example.examine.dto.response.LLM.JournalAnalysis;
import com.example.examine.dto.request.JSERequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.dto.response.LLM.JournalAnalysisWithEffects;
import com.example.examine.dto.response.LLM.LLMJSE;
import com.example.examine.dto.response.LLM.LLMJSEId;
import com.example.examine.dto.response.TableResponse;
import com.example.examine.entity.*;
import com.example.examine.dto.request.JournalRequest;
import com.example.examine.entity.SupplementEffect.*;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.repository.*;
import com.example.examine.repository.JSERepository.JournalSupplementEffectQueryRepository;
import com.example.examine.repository.JSERepository.JournalSupplementEffectRepository;
import com.example.examine.repository.JSERepository.JournalSupplementSideEffectQueryRepository;
import com.example.examine.repository.JSERepository.JournalSupplementSideEffectRepository;
import com.example.examine.repository.JournalRepository.JournalQueryRepository;
import com.example.examine.repository.JournalRepository.JournalRepository;
import com.example.examine.repository.SERepository.SupplementEffectRepository;
import com.example.examine.repository.SERepository.SupplementSideEffectRepository;
import com.example.examine.repository.TagRepository.*;
import com.example.examine.service.Crawler.JournalCrawler.ClinicalTrialsCrawler;
import com.example.examine.service.Crawler.JournalCrawler.JournalCrawlerMeta;
import com.example.examine.service.Crawler.JournalCrawler.PubmedCrawler;
import com.example.examine.service.Crawler.JournalCrawler.SemanticScholarCrawler;
import com.example.examine.service.Crawler.Parser.JournalIdParser;
import com.example.examine.service.LLM.LLMService;
import com.example.examine.service.util.CalculateScore;
import com.example.examine.service.util.EnumService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.examine.service.similarity.TextSimilarity.findTopKSuggestions;
import static com.example.examine.service.util.EnumService.JournalSiteType.fromLink;
import static com.example.examine.service.util.ObjectService.distinctByKey;

// 서비스
@Service
@RequiredArgsConstructor
public class JournalService {
    private static final Logger log = LoggerFactory.getLogger(JournalService.class);

    private final JournalRepository journalRepo;
    private final JournalQueryRepository journalQueryRepo;
    private final TrialDesignRepository trialDesignRepo;
    private final SupplementRepository supplementRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementEffectRepository seRepo;
    private final SupplementSideEffectRepository sseRepo;
    private final JournalSupplementEffectRepository jseRepo;
    private final JournalSupplementEffectQueryRepository jseQueryRepo;
    private final JournalSupplementSideEffectRepository jsseRepo;
    private final JournalSupplementSideEffectQueryRepository jsseQueryRepo;
    private final PageRepository pageRepo;
    private final PubmedCrawler pubmedCrawler;
    private final ClinicalTrialsCrawler clinicalTrialsCrawler;
    private final SemanticScholarCrawler semanticScholarCrawler;
    private final LLMService llmService;
    private final AlarmService alarmService;

    public enum EffectType {
        EFFECT,
        SIDE_EFFECT
    }

    @Transactional
    public ResponseEntity<String> createOne(JournalRequest dto) {
        JournalExtract info = extractJournalInfo(dto.link());
        if (journalRepo.existsBySiteTypeAndSiteJournalId(info.siteType(),  info.siteJournalId())) {
            return ResponseEntity.badRequest().body("이미 같은 논문이 존재합니다.");
        }

        journalRepo.save(create(dto));
        return ResponseEntity.ok("논문 추가 완료");
    }

    public JournalExtract extractJournalInfo(String url) {
        EnumService.JournalSiteType siteType = EnumService.JournalSiteType.fromLink(url);
        String siteJournalId;

        try {
            siteJournalId = switch (siteType) {
                case PUBMED -> JournalIdParser.pubmedJournalId(url);
                case CLINICAL_TRIALS -> JournalIdParser.clinicalTrialsId(url);
                case SEMANTIC_SCHOLAR -> JournalIdParser.semanticScholarId(url);
                default -> throw new IllegalArgumentException("지원하지 않는 논문 링크입니다: " + url);
            };

            if (siteJournalId == null || siteJournalId.isBlank()) {
                throw new IllegalArgumentException("논문 ID 파싱 실패: " + url);
            }

            return new JournalExtract(siteType, siteJournalId);

        } catch (Exception e) {
            log.error("🔎 논문 링크 파싱 실패", e);
            return null;
        }
    }


//    @Transactional
//    public ResponseEntity<String> createBatch(List<JournalRequest> list) {
//        Set<String> existingLinks = journalRepo.findAllByLinkIn(
//                list.stream().map(JournalRequest::link).collect(Collectors.toSet())
//        ).stream().map(Journal::getLink).collect(Collectors.toSet());
//
//        List<Journal> toSave = new ArrayList<>();
//        for (JournalRequest dto : list) {
//            if (!existingLinks.add(dto.link())) continue;
//            try {
//                toSave.add(create(dto));
//            } catch (Exception e) {
//                log.error("논문 생성 실패", e);
//            }
//        }
//        journalRepo.saveAll(toSave);
//        return ResponseEntity.ok(toSave.size() + "건 추가 완료");
//    }


    public Journal create(JournalRequest dto) {

        JournalCrawlerMeta meta = crawlJournalMeta(dto.link());

        Journal journal = Journal.builder()
                .siteType(meta.getSiteType())
                .siteJournalId(meta.getSiteJournalId())
                .durationValue(dto.durationValue())
                .durationUnit(EnumService.DurationUnit.fromString(dto.durationUnit())) // 여기서 String → enum
                .participants(dto.participants())
                .parallel(dto.parallel())
                .blind(dto.blind())
                .build();
        journalRepo.save(journal);

        journal.setDurationDays();

        if (dto.trialDesignId() != null) {
            TrialDesign td = trialDesignRepo.findById(dto.trialDesignId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + dto.trialDesignId()));
            journal.setTrialDesign(td);
        } else {
            journal.setTrialDesign(null);
        }

        journal.setTitle(meta.getTitle());
        if(journal.getParticipants()==null){
            journal.setParticipants(meta.getParticipants());
        }
        journal.setSummary(meta.getSummary());
        journal.setDate(meta.getDate());

        JournalAnalysisWithEffects result = analyze(journal.getTitle(), journal.getSummary());
        applyAnalysis(journal, result, dto);

        syncJSE(journal, dto.effects(), EffectType.EFFECT);
        syncJSE(journal, dto.sideEffects(), EffectType.SIDE_EFFECT);
        return journal;
    }

    public JournalCrawlerMeta crawlJournalMeta(String url) {
        EnumService.JournalSiteType siteType = fromLink(url);
        try {
            return switch (siteType) {
                case PUBMED -> pubmedCrawler.extract(url);
                case CLINICAL_TRIALS -> clinicalTrialsCrawler.extract(url);
                case SEMANTIC_SCHOLAR -> semanticScholarCrawler.extract(url);
                default -> throw new IllegalArgumentException("지원하지 않는 논문 링크입니다.");
            };
        } catch (IOException e) {
            log.error("크롤링 실패", e);
            return new JournalCrawlerMeta(null, null, null, null, null, null);
        }
    }

    public JournalAnalysisWithEffects analyze(String title, String abstractText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();

        String prompt = """
                        You will be given the title and abstract of a scientific paper.
                
                        Your task is to extract the following fields in **strict compact JSON** format with **no explanation or commentary**.
                        If a value is not found, use null. Return only valid JSON (no code block, no preamble).
                
                        {
                          "participants": (integer or null),
                          "durationValue": (integer or null),
                          "durationUnit": "day" | "week" | "month" | "year" | null,
                          "blind": 0 | 1 | 2 | null,  // 0=open-label, 1=single-blind, 2=double-blind
                          "parallel": true | false | null,
                          "design": one of [
                          "Meta-analysis", "Systematic Review", "RCT", "Non-RCT",
                          "Cohort", "Case-control", "Cross-sectional", "Case Report",
                          "Animal Study", "In-vitro Study"
                          ] | null,
                          "effects": [
                             {
                               "supplement": string,              // name of the supplement used (e.g., "N-acetylcysteine")
                                "effect": string,                 // observed effect as a concise noun phrase (e.g., "anxiety", "memory improvement")
                                "cohenD": number or null,         // Only a single numeric value. No ranges, strings, or text.
                                "pearsonR": number or null,       // Must be a valid float number (e.g., 0.56). Do NOT use ranges like "0.5–0.8"
                                "pValue": number or null          // Must be a valid numeric p-value like 0.001. Do NOT use strings.
                             },
                             ...
                          ]
                        }
                
                        Title: %s
                
                        Abstract: %s
                
                """.formatted(title, abstractText);

        try {
            String response = llmService.callLLM(prompt);

            // 1. 일단 전체 파싱
            JournalAnalysis rawAnalysis = mapper.readValue(response, JournalAnalysis.class);

            // 2. effectsRaw 후처리: Map<String, Object> → LLMJSE로 변환
            List<LLMJSE> effects = Optional.ofNullable(rawAnalysis.effectsRaw())
                    .orElse(List.of())
                    .stream()
                    .map(effectMap -> {
                        try {
                            return mapper.convertValue(effectMap, LLMJSE.class);
                        } catch (IllegalArgumentException e) {
                            log.warn("LLMJSE 파싱 실패: {}", effectMap);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            return new JournalAnalysisWithEffects(
                    rawAnalysis.participants(),
                    rawAnalysis.durationValue(),
                    rawAnalysis.durationUnit(),
                    rawAnalysis.blind(),
                    rawAnalysis.parallel(),
                    rawAnalysis.design(),
                    effects
            );

        } catch (Exception e) {
            log.error("LLM 분석 실패", e);
            return new JournalAnalysisWithEffects(null, null, null, null, null, null, List.of());
        }
    }

    // 🔸 applyAnalysisToJournal(): 값이 없을 경우만 반영
    private void applyAnalysis(Journal journal, JournalAnalysisWithEffects result, JournalRequest request) {
        if (journal.getParticipants() == null && result.participants() != null) {
            journal.setParticipants(result.participants());
        }
        if (journal.getDurationDays() == null && result.durationValue()!= null && result.durationUnit() != null) {
            journal.setDurationValue(result.durationValue());
            journal.setDurationUnit(EnumService.DurationUnit.fromString(result.durationUnit()));
            journal.setDurationDays();
        }
        if (journal.getBlind() == null && result.blind() != null) {
            journal.setBlind(result.blind());
        }
        if (journal.getParallel() == null && result.parallel() != null) {
            journal.setParallel(result.parallel());
        }
        if (journal.getTrialDesign() == null && result.design() !=null){
            journal.setTrialDesign(trialDesignRepo.findByEngName(result.design()).orElse(null));
        }
        journal.setScore();
        if ((request.effects() == null||request.effects().isEmpty()) && (request.sideEffects()==null||request.sideEffects().isEmpty())) {
            List<JSERequest> effectRequests = new ArrayList<>();
            List<JSERequest> sideEffectRequests = new ArrayList<>();

            if (result.effects() != null) {
                for (LLMJSE jse : result.effects()) {
                    String supplementKey = jse.supplement();
                    String effectKey = jse.effect();

                    List<TextSimilarityRequest> supplements = findTopKSuggestions(supplementKey, supplementRepo, "supplement", 10, 0.1);
                    List<TextSimilarityRequest> effects = findTopKSuggestions(effectKey, effectRepo, "positive", 10, 0.1);
                    List<TextSimilarityRequest> sideEffects = findTopKSuggestions(effectKey, sideEffectRepo, "negative", 10, 0.1);

                    List<TextSimilarityRequest> allEffects = Stream.concat(effects.stream(), sideEffects.stream())
                            .toList();

                    LLMJSEId mapped = LLMMapping(supplementKey, effectKey, supplements, allEffects);
                    JSERequest req = JSERequest.fromEntity(mapped, jse);

                    if (Objects.equals(mapped.effectType(), "positive")) {
                        effectRequests.add(req);
                    } else {
                        sideEffectRequests.add(req);
                    }
                }

                syncJSE(journal, effectRequests, EffectType.EFFECT);
                syncJSE(journal, sideEffectRequests, EffectType.SIDE_EFFECT);
            }
        }
    }


    public LLMJSEId LLMMapping(
            String originalSupplement,
            String originalEffect,
            List<TextSimilarityRequest> supplements,
            List<TextSimilarityRequest> allEffects
    ) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        root.put("targetSupplement", originalSupplement);
        root.put("targetEffect", originalEffect);

        ArrayNode supplementArray = mapper.createArrayNode();
        for (TextSimilarityRequest s : supplements) {
            ObjectNode o = mapper.createObjectNode();
            o.put("id", s.id());
            o.put("name", s.name());
            o.put("type", s.type());
            supplementArray.add(o);
        }
        root.set("supplementCandidates", supplementArray);

        ArrayNode effectArray = mapper.createArrayNode();
        for (TextSimilarityRequest e : allEffects) {
            ObjectNode o = mapper.createObjectNode();
            o.put("id", e.id());
            o.put("name", e.name());
            o.put("type", e.type());
            effectArray.add(o);
        }
        root.set("effectCandidates", effectArray);

        String prompt = """
    You are given a target supplement and effect extracted from a scientific paper, along with candidate matches from a database.
    Match the most likely supplement and effect (or side effect) from the candidate lists.
    If none of the candidates are a good match, return null for supplementId, effectId, and effectType.
    Return only valid JSON in this format (no code block, no preamble):
    {
      "supplementId": number or null,
      "effectId": number or null,
      "effectType": "positive" | "negative" | null
    }

    Input:
    %s
    """.formatted(root.toString());

        try {
            log.info("💊 Supplements: {}", mapper.writeValueAsString(supplements));
            log.info("🎯 All Effects: {}", mapper.writeValueAsString(allEffects));
            String response = llmService.callLLM(prompt);
            return mapper.readValue(response, LLMJSEId.class);
        } catch (Exception e) {
            log.error("LLM 호출 실패", e);
            throw new RuntimeException("Failed to parse LLM response", e);
        }
    }

        @Transactional
        public ResponseEntity<String> update(Long id, JournalRequest dto) {
            Journal journal = journalRepo.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Journal not found"));
            journal.setBlind(dto.blind());

            if (dto.trialDesignId() != null) {
                TrialDesign td = trialDesignRepo.findById(dto.trialDesignId())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + dto.trialDesignId()));
                journal.setTrialDesign(td);
            } else {
                journal.setTrialDesign(null);
            }

            Long newTrialDesignId = dto.trialDesignId() != null ? dto.trialDesignId() : null;

            TrialDesign newTrialDesign = null;
            if (dto.trialDesignId() != null && newTrialDesignId != null) {
                newTrialDesign = trialDesignRepo.findById(newTrialDesignId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + newTrialDesignId));
                journal.setTrialDesign(newTrialDesign);
            } else {
                journal.setTrialDesign(null);
            }

            journal.setScore();
            // 🔧 새 값 세팅
            journal.setDurationValue(dto.durationValue());
            journal.setDurationUnit(EnumService.DurationUnit.fromString(dto.durationUnit()));
            journal.setDurationDays();
            journal.setBlind(dto.blind());
            journal.setParallel(dto.parallel());
            journal.setParticipants(dto.participants());

            syncJSE(journal, dto.effects(), EffectType.EFFECT);
            syncJSE(journal, dto.sideEffects(), EffectType.SIDE_EFFECT);

            journalRepo.save(journal);
            return ResponseEntity.ok("논문 업데이트 완료");
        }

    private SupplementEffect findOrCreateSupplementEffect(Long supplementId, Long effectId) {
        if (supplementId == null || effectId == null) {
            log.warn("❌ SE ID 생성 불가: supplementId={}, effectId={}", supplementId, effectId);
            return null;
        }

        SupplementEffectId id = new SupplementEffectId(supplementId, effectId);
        return seRepo.findById(id)
                .orElseGet(() -> {
                    log.info("new SE 생성: supplementId:{}, effectId:{}", supplementId, effectId);
                    Supplement supplement = supplementRepo.findById(supplementId).orElse(null);
                    EffectTag effect = effectRepo.findById(effectId).orElse(null);

                    if (supplement == null || effect == null) return null;

                    SupplementEffect newEffect = new SupplementEffect(supplement, effect);
                    seRepo.save(newEffect);
                    return newEffect;
                });
    }


    private SupplementSideEffect findOrCreateSupplementSideEffect(Long supplementId, Long sideEffectId) {
        if (supplementId == null || sideEffectId == null) {
            log.warn("❌ SE ID 생성 불가: supplementId={}, sideEffectId={}", supplementId, sideEffectId);
            return null;
        }

        SupplementSideEffectId id = new SupplementSideEffectId(supplementId, sideEffectId);
        return sseRepo.findById(id)
                .orElseGet(() -> {
                    log.info("new SSE 생성: supplementId:{}, sideEffectId:{}", supplementId, sideEffectId);
                    Supplement supplement = supplementRepo.findById(supplementId).orElse(null);
                    SideEffectTag sideEffect = sideEffectRepo.findById(sideEffectId).orElse(null);

                    if (supplement == null || sideEffect == null) return null;

                    SupplementSideEffect newEntity = new SupplementSideEffect(supplement, sideEffect);
                    sseRepo.save(newEntity);
                    return newEntity;
                });
    }

    public static Integer toDays(Integer value, EnumService.DurationUnit unit) {
        if (value == null) return null;

        return switch (unit) {
            case WEEK -> value * 7;
            case MONTH -> value * 30;  // 평균값 기준
            case YEAR -> value * 365;
            default -> value;
        };
    }


    private void syncJSE(Journal journal, List<JSERequest> requests, EffectType type) {
        if (requests == null) {
            requests = Collections.emptyList();
        }

        // 중복 제거
        List<JSERequest> deduplicatedRequests = requests.stream()
                .filter(req -> req.supplementId() != null && req.effectId() != null)
                .filter(distinctByKey(req -> type == EffectType.EFFECT
                        ? new SupplementEffectId(req.supplementId(), req.effectId())
                        : new SupplementSideEffectId(req.supplementId(), req.effectId())))
                .toList();

        log.info("syncJSE: {}", deduplicatedRequests);


        // ID -> 요청 매핑
        Map<SEId, JSERequest> newRequestMap = deduplicatedRequests.stream()
                .collect(Collectors.toMap(
                        req -> type == EffectType.EFFECT
                                ? new SupplementEffectId(req.supplementId(), req.effectId())
                                : new SupplementSideEffectId(req.supplementId(), req.effectId()),
                        Function.identity(),
                        (a, b) -> b
                ));

        if (type == EffectType.EFFECT) {
            // 기존 JSE 순회 및 삭제/수정
            Iterator<JournalSupplementEffect> iterator = journal.getJournalSupplementEffects().iterator();
            while (iterator.hasNext()) {
                JournalSupplementEffect jse = iterator.next();
                SupplementEffect se = jse.getSE();
                Object id = se.getId();

                JSERequest matchedReq = newRequestMap.remove(id);
                boolean exists = CalculateScore.deleteScore(se, jse);

                if (matchedReq == null) {
                    jseRepo.delete(jse);
                    iterator.remove();
                    if (!exists) {
                        seRepo.delete(se);
                    }
                } else {
                    jse.setCohenD(matchedReq.cohenD());
                    jse.setPearsonR(matchedReq.pearsonR());
                    jse.setPValue(matchedReq.pValue());
                    jse.setParticipants(journal.getParticipants());
                    jse.setScore();
                    CalculateScore.addScore(se, jse);
                }
            }

            // 새로 추가
            for (Map.Entry<SEId, JSERequest> entry : newRequestMap.entrySet()) {
                JSERequest req = entry.getValue();
                SupplementEffect se = findOrCreateSupplementEffect(req.supplementId(), req.effectId());
                if (se == null) continue;

                JournalSupplementEffect newJSE = new JournalSupplementEffect(journal, se, req.cohenD(), req.pearsonR(), req.pValue());
                newJSE.setParticipants(journal.getParticipants());
                newJSE.setScore();

                jseRepo.save(newJSE);
                CalculateScore.addScore(se, newJSE);
                seRepo.save(se);
            }

        } else if (type == EffectType.SIDE_EFFECT) {
            Iterator<JournalSupplementSideEffect> iterator = journal.getJournalSupplementSideEffects().iterator();
            while (iterator.hasNext()) {
                JournalSupplementSideEffect jsse = iterator.next();
                SupplementSideEffect se = jsse.getSE();
                Object id = se.getId();

                JSERequest matchedReq = newRequestMap.remove(id);
                boolean exists = CalculateScore.deleteScore(se, jsse);

                if (matchedReq == null) {
                    jsseRepo.delete(jsse);
                    iterator.remove();
                    if (!exists) {
                        sseRepo.delete(se);
                    }
                } else {
                    jsse.setCohenD(matchedReq.cohenD());
                    jsse.setPearsonR(matchedReq.pearsonR());
                    jsse.setPValue(matchedReq.pValue());
                    jsse.setParticipants(journal.getParticipants());
                    jsse.setScore();
                    CalculateScore.addScore(se, jsse);
                }
            }

            // 새로 추가
            for (Map.Entry<SEId, JSERequest> entry : newRequestMap.entrySet()) {
                JSERequest req = entry.getValue();
                SupplementSideEffect se = findOrCreateSupplementSideEffect(req.supplementId(), req.effectId());
                if (se == null) continue;

                JournalSupplementSideEffect newJSSE = new JournalSupplementSideEffect(journal, se, req.cohenD(), req.pearsonR(), req.pValue());
                newJSSE.setParticipants(journal.getParticipants());
                newJSSE.setScore();

                jsseRepo.save(newJSSE);
                CalculateScore.addScore(se, newJSSE);
                sseRepo.save(se);
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 type: " + type);
        }

        // 알림
        List<Long> supplementIds = newRequestMap.values().stream()
                .map(JSERequest::supplementId)
                .filter(Objects::nonNull)
                .distinct() // 중복 제거
                .toList();

        log.info("알림 대상 supplementIds: {}", supplementIds);
        List<Page> pages = pageRepo.findBySupplementIdsWithSupplement(supplementIds);
        for (Page page: pages) {
            log.info("알림 생성 시도: {}", page.getTitle());

            alarmService.createPageAlarm(page, "논문에 북마크한 성분(" + page.getSupplement().getKorName() + ")이 매핑되었습니다.");
        }
    }



    public List<JournalResponse> toJournalResponses(List<Journal> journals) {
        if (journals.isEmpty()) return List.of();

        List<Long> journalIds = journals.stream().map(Journal::getId).toList();

        // 2. JSE/JSSE는 따로 불러서 맵으로 정리
        List<JournalSupplementEffect> jseList = jseRepo.fetchJSEWithSEByJournalIds(journalIds);
        Map<Long, List<JournalSupplementEffect>> jseMap = jseList.stream()
                .collect(Collectors.groupingBy(jse -> jse.getId().getJournalId()));

        List<JournalSupplementSideEffect> jsseList = jsseRepo.fetchJSSEWithSideEffectByJournalIds(journalIds);
        Map<Long, List<JournalSupplementSideEffect>> jsseMap = jsseList.stream()
                .collect(Collectors.groupingBy(jsse -> jsse.getId().getJournalId()));

        // 3. JournalResponse 조립
        return journals.stream()
                .map(journal -> JournalResponse.fromEntity(
                        journal,
                        jseMap.getOrDefault(journal.getId(), List.of()).stream().map(JSEResponse::fromEntity).toList(),
                        jsseMap.getOrDefault(journal.getId(), List.of()).stream().map(JSEResponse::fromEntity).toList()
                ))
                .toList();
    }

    public TableResponse<JournalResponse> toTableResponse(List<Journal> journals, int limit) {
        boolean hasMore = journals.size() > limit;

        if (hasMore) {
            journals = journals.subList(0, limit);
        }

        return new TableResponse<>(toJournalResponses(journals), hasMore);
    }


    @Transactional(readOnly = true)
    public TableResponse<JournalResponse> sort(String sort, boolean asc, int limit, int offset) {
        List<Journal> journals = journalQueryRepo.findAll(sort, asc, limit + 1, offset);
        return toTableResponse(journals, limit);
    }


    @Transactional(readOnly = true)
    public TableResponse<JournalResponse> search(String keyword, String sort, boolean asc, int limit, int offset) {
        List<Journal> journals = journalQueryRepo.findByKeyword(keyword, sort, asc, limit + 1, offset);
        return toTableResponse(journals, limit);
    }

    @Transactional(readOnly = true)
    public TableResponse<JournalResponse> findFiltered(
            List<Long> trialDesignIds,
            List<Integer> blinds,
            List<Boolean> parallels,
            List<Long> supplementIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            String sort,
            Boolean asc,
            int limit,
            int offset
    ) {
        // 1. 기준 정렬/페이징된 전체 ID
        List<Long> baseIds = journalQueryRepo.findAllIds(sort, asc, limit + 1, offset);
        if (baseIds.isEmpty()) return toTableResponse(List.of(), limit);

        Set<Long> filtered = new HashSet<>(baseIds);

        // 2. filter 조건별 교집합
        if (trialDesignIds != null || blinds != null || parallels != null) {
            List<Long> byDesign = journalQueryRepo.filterByTag(trialDesignIds, blinds, parallels, sort, asc, limit + 1, offset);
            filtered.retainAll(byDesign);
        }

        if (supplementIds != null && !supplementIds.isEmpty()) {
            Set<Long> jseIds = new HashSet<>(jseQueryRepo.findJournalIdsBySupplementIds(supplementIds, sort, asc, limit + 1, offset));
            Set<Long> jsseIds = new HashSet<>(jsseQueryRepo.findJournalIdsBySupplementIds(supplementIds, sort, asc, limit + 1, offset));
            jseIds.addAll(jsseIds); // ∪ (OR 조건)
            filtered.retainAll(jseIds);
        }

        if (effectIds != null && !effectIds.isEmpty()) {
            filtered.retainAll(jseQueryRepo.findJournalIdsByEffectIds(effectIds, sort, asc, limit + 1, offset));
        }

        if (sideEffectIds != null && !sideEffectIds.isEmpty()) {
            filtered.retainAll(jsseQueryRepo.findJournalIdsBySideEffectIds(sideEffectIds, sort, asc, limit + 1, offset));
        }

        // 3. 최종 ID 리스트 (정렬 순서 유지)
        List<Long> finalSortedIds = baseIds.stream()
                .filter(filtered::contains)
                .toList();

        if (finalSortedIds.isEmpty()) return toTableResponse(List.of(), limit);

        // 4. 정렬 객체 생성
        Sort sortObj = Sort.by(asc ? Sort.Direction.ASC : Sort.Direction.DESC, sort);

        // 5. 실제 Journal 조회
        List<Journal> journals = journalRepo.fetchTrialDesignByIds(finalSortedIds, sortObj);
        return toTableResponse(journals, limit);
    }

    @Transactional
    public ResponseEntity<String> delete(Long id) {
        if (!journalRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jseRepo.findWithSEByJournalId(id).forEach(jse->{
            SE se = jse.getSE();
            boolean exists = CalculateScore.deleteScore(se,jse);
            jseRepo.delete((JournalSupplementEffect) jse);
            if(!exists) {
                seRepo.delete((SupplementEffect) se);
            }
        });

        jsseRepo.findWithSEByJournalId(id).forEach(jse->{
            SE se = jse.getSE();
            boolean exists = CalculateScore.deleteScore(se,jse);
            jsseRepo.delete((JournalSupplementSideEffect) jse);
            if(!exists) {
                sseRepo.delete((SupplementSideEffect) se);
            }
        });

        journalRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
