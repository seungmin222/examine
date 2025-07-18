package com.example.examine.service.EntityService;

import com.example.examine.dto.request.LLMJSERequest;
import com.example.examine.dto.response.LLM.JournalAnalysis;
import com.example.examine.dto.request.JSERequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.dto.response.LLM.JournalAnalysisWithEffects;
import com.example.examine.dto.response.LLM.LLMJSE;
import com.example.examine.dto.response.LLM.LLMJSEId;
import com.example.examine.entity.*;
import com.example.examine.dto.request.JournalRequest;
import com.example.examine.entity.SupplementEffect.*;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.repository.*;
import com.example.examine.repository.JSERepository.JournalSupplementEffectRepository;
import com.example.examine.repository.JSERepository.JournalSupplementSideEffectRepository;
import com.example.examine.repository.SERepository.SupplementEffectRepository;
import com.example.examine.repository.SERepository.SupplementSideEffectRepository;
import com.example.examine.repository.TagRepository.*;
import com.example.examine.service.Crawler.JournalCrawler.ClinicalTrialsCrawler;
import com.example.examine.service.Crawler.JournalCrawler.JournalCrawlerMeta;
import com.example.examine.service.Crawler.JournalCrawler.PubmedCrawler;
import com.example.examine.service.Crawler.JournalCrawler.SemanticScholarCrawler;
import com.example.examine.service.LLM.LLMService;
import com.example.examine.service.similarity.TextSimilarity;
import com.example.examine.service.util.CalculateScore;
import com.example.examine.service.util.EnumService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
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

import static com.example.examine.service.util.ObjectService.distinctByKey;

// 서비스
@Service
@RequiredArgsConstructor
public class JournalService {
    private static final Logger log = LoggerFactory.getLogger(JournalService.class);

    private final JournalRepository journalRepo;
    private final TrialDesignRepository trialDesignRepo;
    private final SupplementRepository supplementRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final JournalSupplementEffectRepository jseRepo;
    private final JournalSupplementSideEffectRepository jsseRepo;
    private final PubmedCrawler pubmedCrawler;
    private final ClinicalTrialsCrawler clinicalTrialsCrawler;
    private final SemanticScholarCrawler semanticScholarCrawler;
    private final LLMService llmService;
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public ResponseEntity<String> createOne(JournalRequest dto) {
        if (journalRepo.findByLink(dto.link()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 논문이 존재합니다.");
        }

        journalRepo.save(create(dto));
        return ResponseEntity.ok("논문 추가 완료");
    }

    @Transactional
    public ResponseEntity<String> createBatch(List<JournalRequest> list) {
        Set<String> existingLinks = journalRepo.findAllByLinkIn(
                list.stream().map(JournalRequest::link).collect(Collectors.toSet())
        ).stream().map(Journal::getLink).collect(Collectors.toSet());

        List<Journal> toSave = new ArrayList<>();
        for (JournalRequest dto : list) {
            if (!existingLinks.add(dto.link())) continue;
            try {
                toSave.add(create(dto));
            } catch (Exception e) {
                log.error("논문 생성 실패", e);
            }
        }
        journalRepo.saveAll(toSave);
        return ResponseEntity.ok(toSave.size() + "건 추가 완료");
    }


    public Journal create(JournalRequest dto) {
        Journal journal = Journal.builder()
                .link(dto.link())
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

        JournalCrawlerMeta meta = crawlJournalMeta(dto.link());

        journal.setTitle(meta.getTitle());
        if(journal.getParticipants()==null){
            journal.setParticipants(meta.getParticipants());
        }
        journal.setSummary(meta.getSummary());
        journal.setDate(meta.getDate());

        JournalAnalysisWithEffects result = analyze(journal.getTitle(), journal.getSummary());
        applyAnalysis(journal, result, dto);

        syncJSE(journal, dto.effects());
        syncJSSE(journal, dto.sideEffects());
        return journal;
    }

    public JournalCrawlerMeta crawlJournalMeta(String url) {
        try {
            if (url.contains("pubmed.ncbi.nlm.nih.gov")) {
                return pubmedCrawler.extract(url);
            } else if (url.contains("clinicaltrials.gov")) {
                return clinicalTrialsCrawler.extract(url);
            } else if (url.contains("semanticscholar")) {
                return semanticScholarCrawler.extract(url);
            } else {
                throw new IllegalArgumentException("지원하지 않는 논문 링크입니다.");
            }
        } catch (IOException e) {
            log.error("크롤링 실패", e);
        }
        return new JournalCrawlerMeta(null,null,null,null);
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
            journal.setDurationUnit(result.durationUnit());
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

                    List<LLMJSERequest> supplements = findTopKSuggestions(supplementKey, supplementRepo, "supplement");
                    List<LLMJSERequest> effects = findTopKSuggestions(effectKey, effectRepo, "positive");
                    List<LLMJSERequest> sideEffects = findTopKSuggestions(effectKey, sideEffectRepo, "negative");

                    List<LLMJSERequest> allEffects = Stream.concat(effects.stream(), sideEffects.stream())
                            .toList();

                    LLMJSEId mapped = LLMMapping(supplementKey, effectKey, supplements, allEffects);
                    JSERequest req = JSERequest.fromEntity(mapped, jse);

                    if (Objects.equals(mapped.effectType(), "positive")) {
                        effectRequests.add(req);
                    } else {
                        sideEffectRequests.add(req);
                    }
                }

                syncJSE(journal, effectRequests);
                syncJSSE(journal, sideEffectRequests);
            }
        }
    }

    private List<LLMJSERequest> findTopKSuggestions(
            String targetText,
            TagRepository<? extends Tag> repository,
            String type
    ) {
        int k = 10;
        double threshold = 0.1;

        PriorityQueue<AbstractMap.SimpleEntry<LLMJSERequest, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue)
        );

        repository.findAll().forEach(tag -> {
            double score = TextSimilarity.combinedScore(tag.getEngName(), targetText);
            LLMJSERequest req = new LLMJSERequest(tag.getId(), tag.getEngName(), type);
            pqAddTopK(pq, req, score, k);
        });

        return pq.stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 높은 순으로
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }


    private void pqAddTopK(
            PriorityQueue<AbstractMap.SimpleEntry<LLMJSERequest, Double>> pq,
            LLMJSERequest item,
            double score,
            int k
    ) {
        if (pq.size() < k) {
            pq.add(new AbstractMap.SimpleEntry<>(item, score));
        } else if (pq.peek().getValue() < score) {
            pq.poll();
            pq.add(new AbstractMap.SimpleEntry<>(item, score));
        }
    }

    public LLMJSEId LLMMapping(
            String originalSupplement,
            String originalEffect,
            List<LLMJSERequest> supplements,
            List<LLMJSERequest> allEffects
    ) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        root.put("targetSupplement", originalSupplement);
        root.put("targetEffect", originalEffect);

        ArrayNode supplementArray = mapper.createArrayNode();
        for (LLMJSERequest s : supplements) {
            ObjectNode o = mapper.createObjectNode();
            o.put("id", s.id());
            o.put("name", s.name());
            o.put("type", s.type());
            supplementArray.add(o);
        }
        root.set("supplementCandidates", supplementArray);

        ArrayNode effectArray = mapper.createArrayNode();
        for (LLMJSERequest e : allEffects) {
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
    Return only valid JSON in this format(no code block, no preamble).:
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
            journal.setDurationUnit(dto.durationUnit());
            journal.setDurationDays();
            journal.setBlind(dto.blind());
            journal.setParallel(dto.parallel());
            journal.setParticipants(dto.participants());

            syncJSE(journal, dto.effects());
            syncJSSE(journal, dto.sideEffects());

            journalRepo.save(journal);
            return ResponseEntity.ok("논문 업데이트 완료");
        }

    private SupplementEffect findOrCreateSupplementEffect(Long supplementId, Long effectId) {
        if (supplementId == null || effectId == null) {
            log.warn("❌ SE ID 생성 불가: supplementId={}, effectId={}", supplementId, effectId);
            return null;
        }

        SupplementEffectId id = new SupplementEffectId(supplementId, effectId);
        return supplementEffectRepo.findById(id)
                .orElseGet(() -> {
                    log.info("new SE 생성: supplementId:{}, effectId:{}", supplementId, effectId);
                    Supplement supplement = supplementRepo.findById(supplementId).orElse(null);
                    EffectTag effect = effectRepo.findById(effectId).orElse(null);

                    if (supplement == null || effect == null) return null;

                    SupplementEffect newEffect = new SupplementEffect(supplement, effect);
                    supplementEffectRepo.save(newEffect);
                    return newEffect;
                });
    }


    private SupplementSideEffect findOrCreateSupplementSideEffect(Long supplementId, Long sideEffectId) {
        if (supplementId == null || sideEffectId == null) {
            log.warn("❌ SE ID 생성 불가: supplementId={}, sideEffectId={}", supplementId, sideEffectId);
            return null;
        }

        SupplementSideEffectId id = new SupplementSideEffectId(supplementId, sideEffectId);
        return supplementSideEffectRepo.findById(id)
                .orElseGet(() -> {
                    log.info("new SSE 생성: supplementId:{}, sideEffectId:{}", supplementId, sideEffectId);
                    Supplement supplement = supplementRepo.findById(supplementId).orElse(null);
                    SideEffectTag sideEffect = sideEffectRepo.findById(sideEffectId).orElse(null);

                    if (supplement == null || sideEffect == null) return null;

                    SupplementSideEffect newEntity = new SupplementSideEffect(supplement, sideEffect);
                    supplementSideEffectRepo.save(newEntity);
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


    private void syncJSE(Journal journal, List<JSERequest> requests) {
        // 1. 요청으로부터 새롭게 들어온 ID 쌍들 추출
        if (requests == null) {
            requests = Collections.emptyList();
        }

        List<JSERequest> deduplicatedRequests = requests.stream()
                .filter(req -> req.supplementId() != null && req.effectId() != null)
                .filter(distinctByKey(req -> new SupplementEffectId(req.supplementId(), req.effectId())))
                .toList();

        log.info("syncJSE: {}",deduplicatedRequests);

        Map<SupplementEffectId, JSERequest> newRequestMap = deduplicatedRequests.stream()
                .filter(req -> req.supplementId() != null && req.effectId() != null)
                .collect(Collectors.toMap(
                        req -> new SupplementEffectId(req.supplementId(), req.effectId()),
                        Function.identity(),
                        (a, b) -> b
                ));

        // 2. 기존 JSE들을 순회하며 삭제 or 수정 결정
        Iterator<JournalSupplementEffect> iterator = journal.getJournalSupplementEffects().iterator();
        while (iterator.hasNext()) {

            JournalSupplementEffect jse = iterator.next();
            SupplementEffectId id = jse.getSE().getId();
            log.info("syncJSE/ existing JSE: JournalId={}, SEId={}", jse.getJournal().getId(), id);

            JSERequest matchedReq = newRequestMap.remove(id); // 있으면 꺼내서 처리하고 제거
            SupplementEffect se = jse.getSE();
            boolean exists = CalculateScore.deleteScore(se, jse);

            if (matchedReq == null) {
                jseRepo.delete(jse);
                iterator.remove();
                if(!exists) {
                    supplementEffectRepo.delete(se);
                }
            } else {
                // 🔄 점수만 업데이트
                jse.setCohenD(matchedReq.cohenD());
                jse.setPearsonR(matchedReq.pearsonR());
                jse.setPValue(matchedReq.pValue());
                jse.setParticipants(journal.getParticipants());
                jse.setScore();
                CalculateScore.addScore(se, jse);
            }
        }

        // 3. newRequestMap에 남은 것들은 → 새로 추가된 ID 쌍
            for (Map.Entry<SupplementEffectId, JSERequest> entry : newRequestMap.entrySet()) {
                JSERequest req = entry.getValue();
                log.info("syncJSE/ supplementId: {}, effectId: {}",req.supplementId(), req.effectId());
                SupplementEffect se = findOrCreateSupplementEffect(req.supplementId(), req.effectId());
                if(se==null) {
                    continue;
                }

                JournalSupplementEffect newJSE = new JournalSupplementEffect(
                        journal, se, req.cohenD(), req.pearsonR(), req.pValue()
                );
                log.info("new JournalSupplementEffect created: journalId={}, supplementId={}, effectId={}",
                        journal.getId(), se.getId().getSupplementId(), se.getId().getEffectId());
                newJSE.setParticipants(journal.getParticipants());
                newJSE.setScore();

                jseRepo.save(newJSE);
                CalculateScore.addScore(se, newJSE);
                supplementEffectRepo.save(se);
            }
    }


    private void syncJSSE(Journal journal, List<JSERequest> requests) {
        // 1. 요청에서 새로운 ID 쌍들 추출
        if (requests == null) {
            requests = Collections.emptyList();
        }

        List<JSERequest> deduplicatedRequests = requests.stream()
                .filter(req -> req.supplementId() != null && req.effectId() != null)
                .filter(distinctByKey(req -> new SupplementSideEffectId(req.supplementId(), req.effectId())))
                .toList();

        log.info("syncJSSE: {}",deduplicatedRequests);
        Map<SupplementSideEffectId, JSERequest> newRequestMap = deduplicatedRequests.stream()
                .filter(req -> req.supplementId() != null && req.effectId() != null)
                .collect(Collectors.toMap(
                        req -> new SupplementSideEffectId(req.supplementId(), req.effectId()),
                        Function.identity(),
                        (a, b) -> b
                ));

        // 2. 기존 JSSE들을 순회하며 삭제 or 수정
        Iterator<JournalSupplementSideEffect> iterator = journal.getJournalSupplementSideEffects().iterator();
        while (iterator.hasNext()) {
            JournalSupplementSideEffect jsse = iterator.next();
            SupplementSideEffectId id = jsse.getSE().getId();

            JSERequest matchedReq = newRequestMap.remove(id);
            SupplementSideEffect se = jsse.getSE();
            boolean exists = CalculateScore.deleteScore(se, jsse);

            if (matchedReq == null) {
                jsseRepo.delete(jsse);
                iterator.remove();
                if(!exists) {
                    supplementSideEffectRepo.delete(se);
                }
            } else {
                // 🔄 점수만 업데이트
                jsse.setCohenD(matchedReq.cohenD());
                jsse.setPearsonR(matchedReq.pearsonR());
                jsse.setPValue(matchedReq.pValue());
                jsse.setParticipants(journal.getParticipants());
                jsse.setScore();
                CalculateScore.addScore(se, jsse);
            }
        }

        // 3. 남은 것들 추가
        for (Map.Entry<SupplementSideEffectId, JSERequest> entry : newRequestMap.entrySet()) {
            JSERequest req = entry.getValue();
            SupplementSideEffect se = findOrCreateSupplementSideEffect(req.supplementId(), req.effectId());
            if(se==null) {
                continue;
            }
            JournalSupplementSideEffect newJSSE = new JournalSupplementSideEffect(
                    journal, se, req.cohenD(), req.pearsonR(), req.pValue()
            );
            log.info("new JournalSupplementSideEffect created: journalId={}, supplementId={}, effectId={}",
                    journal.getId(), se.getId().getSupplementId(), se.getId().getEffectId());
            newJSSE.setParticipants(journal.getParticipants());
            newJSSE.setScore();

            jsseRepo.save(newJSSE);
            CalculateScore.addScore(se, newJSSE);
            supplementSideEffectRepo.save(se);
        }
    }


    public List<JournalResponse> toJournalResponses(List<Long> ids, Sort sort) {
        if (ids.isEmpty()) return List.of();
        List<Journal> journals = journalRepo.fetchTrialDesignByIds(ids, sort);
        journalRepo.fetchEffectsByIds(ids);
        journalRepo.fetchSideEffectsByIds(ids);

        return journals.stream()
                .map(JournalResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JournalResponse> sort(Sort sort) {
        List<Long> journalIds = journalRepo.findAllId();
        return toJournalResponses(journalIds, sort);
    }

    @Transactional(readOnly = true)
    public List<JournalResponse> search(String keyword, Sort sort) {
        List<Long> journalIds = journalRepo.findIdsByKeyword(keyword);
        return toJournalResponses(journalIds, sort);
    }

    @Transactional(readOnly = true)
    public List<JournalResponse> findFiltered(
            List<Long> trialDesign,
            Integer blind,
            Boolean parallel,
            List<Long> supplementIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            Sort sort
    ) {
        List<Long> baseIds = journalRepo.findIdsByBasic(trialDesign, blind, parallel);
        if (baseIds.isEmpty()) return List.of(); // early return 최적화

        Set<Long> filtered = new HashSet<>(baseIds);

        // 🔹 supplementIds는 jse + jsse 둘 다 union
        if (supplementIds != null) {
            Set<Long> supplementMatched = new HashSet<>(jseRepo.findJournalIdsBySupplementIds(supplementIds));
            supplementMatched.addAll(jsseRepo.findJournalIdsBySupplementIds(supplementIds));
            filtered.retainAll(supplementMatched);
        }

        // 🔹 효과별 필터링
        if (effectIds != null) {
            List<Long> matchedByEffect = jseRepo.findJournalIdsByEffectIds(effectIds);
            filtered.retainAll(matchedByEffect);
        }

        // 🔹 부작용 필터링
        if (sideEffectIds != null) {
            List<Long> matchedBySideEffect = jsseRepo.findJournalIdsBySideEffectIds(sideEffectIds);
            filtered.retainAll(matchedBySideEffect);
        }

        return toJournalResponses(filtered.stream().toList(), sort);
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
                supplementEffectRepo.delete((SupplementEffect) se);
            }
        });

        jsseRepo.findWithSEByJournalId(id).forEach(jse->{
            SE se = jse.getSE();
            boolean exists = CalculateScore.deleteScore(se,jse);
            jsseRepo.delete((JournalSupplementSideEffect) jse);
            if(!exists) {
                supplementSideEffectRepo.delete((SupplementSideEffect) se);
            }
        });

        journalRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
