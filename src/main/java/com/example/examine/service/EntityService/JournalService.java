package com.example.examine.service.EntityService;

import com.example.examine.dto.request.LLMJSERequest;
import com.example.examine.dto.response.JSEResponse;
import com.example.examine.dto.response.LLM.JournalAnalysis;
import com.example.examine.dto.request.JSERequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.dto.response.LLM.LLMJSE;
import com.example.examine.dto.response.LLM.LLMJSEId;
import com.example.examine.entity.*;
import com.example.examine.dto.request.JournalRequest;
import com.example.examine.entity.SupplementEffect.SupplementEffectId;
import com.example.examine.entity.SupplementEffect.SupplementSideEffectId;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffectId;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffectId;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.repository.*;
import com.example.examine.service.crawler.ClinicalTrialsCrawler;
import com.example.examine.service.crawler.JournalCrawlerMeta;
import com.example.examine.service.crawler.PubmedCrawler;
import com.example.examine.service.crawler.SemanticScholarCrawler;
import com.example.examine.service.llm.LLMService;
import com.example.examine.service.similarity.TextSimilarity;
import com.example.examine.service.util.CalculateScore;
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
    public ResponseEntity<String> create(JournalRequest dto) {
        if (journalRepo.findByLink(dto.link()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 논문이 존재합니다.");
        }

        Journal journal = Journal.builder()
                .link(dto.link())
                .durationValue(dto.durationValue())
                .durationUnit(dto.durationUnit())
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


        syncJSE(journal, dto.effects());
        syncJSSE(journal, dto.sideEffects());

        JournalCrawlerMeta meta = crawlJournalMeta(dto.link());

        journal.setTitle(meta.getTitle());
        if(journal.getParticipants()==null){
            journal.setParticipants(meta.getParticipants());
        }
        journal.setSummary(meta.getSummary());
        journal.setDate(meta.getDate());

        JournalAnalysis result = analyze(journal.getTitle(), journal.getSummary());
        applyAnalysis(journal, result);


        journal.setScore();
        journalRepo.save(journal);
        return ResponseEntity.ok("논문 추가 완료");
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

    public JournalAnalysis analyze(String title, String abstractText) {
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
            return mapper.readValue(response, JournalAnalysis.class);
        } catch (Exception e) {
            // 예외 로깅하고, 실패 시 기본 객체 반환
            log.error("LLM 분석 실패", e);
            return new JournalAnalysis(null, null, null, null, null, null, null);
        }
    }

    // 🔸 applyAnalysisToJournal(): 값이 없을 경우만 반영
    private void applyAnalysis(Journal journal, JournalAnalysis result) {
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
        if (journal.getJournalSupplementEffects().isEmpty() && journal.getJournalSupplementSideEffects().isEmpty()) {
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
                    JSERequest request = JSERequest.fromEntity(mapped, jse);

                    if (Objects.equals(mapped.effectType(), "positive")) {
                        effectRequests.add(request);
                    } else {
                        sideEffectRequests.add(request);
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
        double threshold = 0.5;

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
        Journal OldJournal = journal;
        journal.setBlind(dto.blind());

        if (dto.trialDesignId() != null) {
            TrialDesign td = trialDesignRepo.findById(dto.trialDesignId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + dto.trialDesignId()));
            journal.setTrialDesign(td);
        } else {
            journal.setTrialDesign(null);
        }
        Integer days = toDays(dto.durationValue(), dto.durationUnit());
        Integer oldDuration = journal.getDurationDays() != null ? journal.getDurationDays() : 1;
        Integer oldParticipants = journal.getParticipants() != null ? journal.getParticipants() : 1;

        Long oldTrialDesignId = journal.getTrialDesign() != null ? journal.getTrialDesign().getId() : null;
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

        // ✅ 변경사항 비교 후 score 재계산
        boolean journalChanged =
                oldParticipants != dto.participants()
                        || oldDuration != days
                        || journal.getBlind() != dto.blind()
                        || !Objects.equals(oldTrialDesignId, newTrialDesignId);


        // 🔧 새 값 세팅
        journal.setDurationValue(dto.durationValue());
        journal.setDurationUnit(dto.durationUnit());
        journal.setDurationDays();
        journal.setBlind(dto.blind());
        journal.setParallel(dto.parallel());
        journal.setParticipants(dto.participants());


        if (journalChanged) {
            List<JournalSupplementEffect> effects = jseRepo.findByJournalId(id);
            List<JournalSupplementSideEffect> sideEffects = jsseRepo.findAllByJournalId(id);
            for (JournalSupplementEffect jse : effects) {
                jse.setScore(); // 내부에서 재계산, setScore() 내부에서 SupplementEffect에 영향 반영
            }
            for (JournalSupplementSideEffect jse : sideEffects) {
                jse.setScore(); // 내부에서 재계산, setScore() 내부에서 SupplementEffect에 영향 반영
            }
        }

        syncJSE(journal, dto.effects());
        syncJSSE(journal, dto.sideEffects());

        journalRepo.save(journal);
        return ResponseEntity.ok("논문 추가 완료");
    }

    private SupplementEffect findOrCreateSupplementEffect(Long supplementId, Long effectId) {
        return supplementEffectRepo.findBySupplementIdAndEffectTagId(supplementId, effectId)
                .orElseGet(() -> {
                    Supplement supplement = supplementRepo.findById(supplementId)
                            .orElseThrow(() -> new EntityNotFoundException("해당 supplement가 존재하지 않습니다."));
                    EffectTag effect = effectRepo.findById(effectId)
                            .orElseThrow(() -> new EntityNotFoundException("해당 effectTag가 존재하지 않습니다."));
                    SupplementEffect newEffect = new SupplementEffect(supplement, effect);
                    return supplementEffectRepo.save(newEffect);
                });
    }

    private SupplementSideEffect findOrCreateSupplementSideEffect(Long supplementId, Long sideEffectId) {
        return supplementSideEffectRepo.findBySupplementIdAndSideEffectTagId(supplementId, sideEffectId)
                .orElseGet(() -> {
                    Supplement supplement = supplementRepo.findById(supplementId)
                            .orElseThrow(() -> new EntityNotFoundException("해당 supplement가 존재하지 않습니다."));
                    SideEffectTag sideEffect = sideEffectRepo.findById(sideEffectId)
                            .orElseThrow(() -> new EntityNotFoundException("해당 sideEffect가 존재하지 않습니다."));
                    SupplementSideEffect newEntity = new SupplementSideEffect(supplement, sideEffect);
                    return supplementSideEffectRepo.save(newEntity);
                });
    }


    public static Integer toDays(Integer value, String unit) {
        if(value==null||unit==null) {
            return null;
        }
        return switch (unit.toLowerCase()) {
            case "day", "days" -> value;
            case "week", "weeks" -> value * 7;
            case "month", "months" -> value * 30;  // 평균 기준
            case "year", "years" -> value * 365;
            default -> throw new IllegalArgumentException("Invalid duration unit: " + unit);
        };
    }

    private void syncJSE(Journal journal, List<JSERequest> requests) {
        // 기존 JSE 전부 제거
        journal.getJournalSupplementEffects().forEach(jse -> {
            SupplementEffect se = jse.getSE();
            if (!CalculateScore.deleteScore(se, jse)) {
                supplementEffectRepo.deleteById(se.getId());
            }
            jseRepo.delete(jse);
        });
        jseRepo.flush(); // DB에 삭제 즉시 반영(request.effect랑 겹칠 경우 업데이트로 로직 분리도 고려)
        entityManager.clear(); // 세션 초기화
        journal.getJournalSupplementEffects().clear();

        Set<SupplementEffectId> idSet = new HashSet<>();
        for (JSERequest req : requests) {
            if(req.supplementId()==null || req.effectId()==null) {
                continue;
            }
            SupplementEffectId id = new SupplementEffectId(req.supplementId(), req.effectId());
            if (!idSet.add(id)) {
                continue;
            }
            SupplementEffect se = findOrCreateSupplementEffect(req.supplementId(), req.effectId());

            JournalSupplementEffect newJSE = new JournalSupplementEffect(journal, se, req.cohenD(), req.pearsonR(), req.pValue());
            newJSE.setParticipants(journal.getParticipants());
            newJSE.setScore();
            CalculateScore.addScore(se, newJSE);
            journal.getJournalSupplementEffects().add(newJSE); // 양방향 연관관계 유지
            jseRepo.save(newJSE);
        }
    }

    private void syncJSSE(Journal journal, List<JSERequest> requests) {
        // 기존 JSSE 전부 제거
        journal.getJournalSupplementSideEffects().forEach(jsse -> {
            SupplementSideEffect se = jsse.getSE();
            if (!CalculateScore.deleteScore(se, jsse)) {
                supplementSideEffectRepo.deleteById(se.getId());
            }
            jsseRepo.delete(jsse);
        });
        jsseRepo.flush(); // 삭제 즉시 반영
        entityManager.clear(); // 세션 초기화
        journal.getJournalSupplementSideEffects().clear();

        Set<SupplementSideEffectId> idSet = new HashSet<>();
        for (JSERequest req : requests) {
            if(req.supplementId()==null || req.effectId()==null) {
                continue;
            }
            SupplementSideEffectId id = new SupplementSideEffectId(req.supplementId(), req.effectId());
            if (!idSet.add(id)) {
                continue;
            }
            SupplementSideEffect se = findOrCreateSupplementSideEffect(req.supplementId(), req.effectId());

            JournalSupplementSideEffect newJSSE = new JournalSupplementSideEffect(journal, se, req.cohenD(), req.pearsonR(), req.pValue());
            newJSSE.setParticipants(journal.getParticipants());
            newJSSE.setScore();
            CalculateScore.addScore(se, newJSSE);
            journal.getJournalSupplementSideEffects().add(newJSSE);
            jsseRepo.save(newJSSE);
        }
    }


    public List<JournalResponse> toJournalResponses(List<Journal> journals) {
        if (journals.isEmpty()) return List.of();

        List<Long> journalIds = journals.stream().map(Journal::getId).toList();

        Map<Long, List<JSEResponse>> jseMap = jseRepo.findAllByJournalIdIn(journalIds).stream()
                .collect(Collectors.groupingBy(JSEResponse::journalId));

        Map<Long, List<JSEResponse>> jsseMap = jsseRepo.findAllByJournalIdIn(journalIds).stream()
                .collect(Collectors.groupingBy(JSEResponse::journalId));

        return journals.stream()
                .map(j -> JournalResponse.fromEntity(
                        j,
                        jseMap.getOrDefault(j.getId(), List.of()),
                        jsseMap.getOrDefault(j.getId(), List.of())
                ))
                .toList();
    }


    public List<JournalResponse> sort(Sort sort) {
        List<Journal> journals = journalRepo.findAllBasic(sort);
        return toJournalResponses(journals);
    }

    public List<JournalResponse> search(String keyword, Sort sort) {
        List<Journal> journals = journalRepo.findByTitleContainingIgnoreCase(keyword, sort);
        return toJournalResponses(journals);
    }

    public List<JournalResponse> findFiltered(
            List<Long> trialDesign,
            Integer blind,
            Boolean parallel,
            List<Long> supplementIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            Sort sort
    ) {
        List<Journal> journals = journalRepo.findFiltered(
                trialDesign, blind, parallel, supplementIds, effectIds, sideEffectIds, sort
        );
        return toJournalResponses(journals);
    }


    public ResponseEntity<String> delete(Long id) {
        if (!journalRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        journalRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
