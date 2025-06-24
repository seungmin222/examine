package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.response.JournalAnalysis;
import com.example.examine.dto.request.JSERequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.entity.*;
import com.example.examine.dto.request.JournalRequest;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.Effect.SideEffectTag;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffectId;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffectId;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import com.example.examine.repository.*;
import com.example.examine.service.crawler.ClinicalTrialsCrawler;
import com.example.examine.service.crawler.JournalCrawlerMeta;
import com.example.examine.service.crawler.PubmedCrawler;
import com.example.examine.service.crawler.SemanticScholarCrawler;
import com.example.examine.service.llm.LLMResponse;
import com.example.examine.service.llm.LLMService;
import com.example.examine.service.util.CalculateScore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.examine.service.llm.LLMService.objectToJsonString;

// 서비스
@Service
public class JournalService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final JournalRepository journalRepo;
    private final TrialDesignRepository trialDesignRepo;
    private final SupplementRepository supplementRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final JournalSupplementEffectRepository journalSupplementEffectRepo;
    private final JournalSupplementSideEffectRepository journalSupplementSideEffectRepo;
    private final PubmedCrawler pubmedCrawler;
    private final ClinicalTrialsCrawler clinicalTrialsCrawler;
    private final SemanticScholarCrawler semanticScholarCrawler;

    public JournalService(JournalRepository journalRepo,
                          TrialDesignRepository trialDesignRepo,
                          SupplementRepository supplementRepo,
                          EffectTagRepository effectRepo,
                          SideEffectTagRepository sideEffectRepo,
                          SupplementEffectRepository supplementEffectRepo,
                          SupplementSideEffectRepository supplementSideEffectRepo,
                          JournalSupplementEffectRepository journalSupplementEffectRepo,
                          JournalSupplementSideEffectRepository journalSupplementSideEffectRepo,
                          PubmedCrawler pubmedCrawler,
                          ClinicalTrialsCrawler clinicalTrialsCrawler,
                          SemanticScholarCrawler semanticScholarCrawler) {
        this.journalRepo = journalRepo;
        this.trialDesignRepo = trialDesignRepo;
        this.supplementRepo = supplementRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.supplementEffectRepo = supplementEffectRepo;
        this.supplementSideEffectRepo = supplementSideEffectRepo;
        this.journalSupplementEffectRepo = journalSupplementEffectRepo;
        this.journalSupplementSideEffectRepo = journalSupplementSideEffectRepo;
        this.pubmedCrawler = pubmedCrawler;
        this.clinicalTrialsCrawler = clinicalTrialsCrawler;
        this.semanticScholarCrawler = semanticScholarCrawler;
    }

    @Transactional
    public ResponseEntity<?> create(JournalRequest dto) {
        if (journalRepo.findByLink(dto.link()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 논문이 존재합니다.");
        }

        Journal journal = new Journal();
        journal.setLink(dto.link());
        journal.setDurationValue(dto.durationValue());
        journal.setDurationUnit(dto.durationUnit());
        journal.setDurationDays();
        journal.setParticipants(dto.participants());
        journal.setParallel(dto.parallel());
        journal.setBlind(dto.blind());

        if (dto.trialDesignId() != null) {
            TrialDesign td = trialDesignRepo.findById(dto.trialDesignId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + dto.trialDesignId()));
            journal.setTrialDesign(td);
        } else {
            journal.setTrialDesign(null);
        }

        syncJSE(journal, null, dto.effects());
        syncJSSE(journal, null, dto.sideEffects());

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
        return ResponseEntity.ok(journalRepo.save(journal));
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
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt = """
        You will be given an abstract of a scientific paper. Your task is to extract the following fields in strict JSON format with no explanation or commentary. If a value is not found, use null.

        No explanation, no preamble, no code block.
        Return only valid compact JSON. :

        {
          "participants": (integer or null),
          "durationValue": (integer or null),
          "durationUnit": "day" | "week" | "month" | "year" | null,
          "blind": (0=open-label, 1=single-blind, 2=double-blind, or null),
          "parallel": (true/false/null),
          "design": one of the following strings, or null:
            [
              "Meta-analysis", "Systematic Review", "RCT", "Non-RCT",
              "Cohort", "Case-control", "Cross-sectional", "Case Report",
              "Animal Study", "In-vitro Study", "Unknown"
            ]
        }

        Title: %s
        
        Abstract: %s
        """.formatted(title, abstractText);

        try {
            String jsonPrompt = LLMService.objectToJsonString(prompt);

            String requestBody = """
        {
          "model": "llama3",
          "prompt": %s,
          "stream": false
        }
        """.formatted(jsonPrompt);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:11434/api/generate", entity, String.class);

            ObjectMapper mapper = new ObjectMapper();

            LLMResponse wrapper = mapper.readValue(response.getBody(), LLMResponse.class);

            String raw = wrapper.response;
            if (!raw.trim().endsWith("}")) {
                raw += "}";
            }

            System.out.println("LLM 응답: " + raw);
            return mapper.readValue(raw, JournalAnalysis.class);

        } catch (Exception e) {
            log.error("LLM 분석 실패", e);// 혹은 logger.error("LLM 분석 실패", e);
        }
        return new JournalAnalysis(null,null,null,null,null,null);
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
            journal.setTrialDesign(trialDesignRepo.findByName(result.design()).orElse(null));
        }
    }


    @Transactional
    public ResponseEntity<?> update(Long id, JournalRequest dto) {
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
            List<JournalSupplementEffect> effects = journalSupplementEffectRepo.findAllByJournalId(id);
            List<JournalSupplementSideEffect> sideEffects = journalSupplementSideEffectRepo.findAllByJournalId(id);
            for (JournalSupplementEffect jse : effects) {
                SupplementEffect se = findOrCreateSupplementEffect(
                        jse.getSupplement().getId(),
                        jse.getEffect().getId()
                );
                jse.setScore(); // 내부에서 재계산, setScore() 내부에서 SupplementEffect에 영향 반영
            }
            for (JournalSupplementSideEffect jse : sideEffects) {
                SupplementSideEffect se = findOrCreateSupplementSideEffect(
                        jse.getSupplement().getId(),
                        jse.getEffect().getId()
                );
                jse.setScore(); // 내부에서 재계산, setScore() 내부에서 SupplementEffect에 영향 반영
            }
        }

        syncJSE(journal, OldJournal, dto.effects());
        syncJSSE(journal, OldJournal, dto.sideEffects());

        return ResponseEntity.ok(journalRepo.save(journal));
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

    private void syncJSE(Journal journal, Journal oldJournal, List<JSERequest> requests) {
        // 데이터 미리 로딩
        Map<Long, Supplement> supplementMap = supplementRepo.findAllById(
                        requests.stream().map(JSERequest::supplementId).toList())
                .stream().collect(Collectors.toMap(Supplement::getId, Function.identity()));

        Map<Long, EffectTag> effectMap = effectRepo.findAllById(
                        requests.stream().map(JSERequest::effectId).toList())
                .stream().collect(Collectors.toMap(EffectTag::getId, Function.identity()));

        if (oldJournal == null) {
            for (JSERequest req : requests) {
                JournalSupplementEffectId id = new JournalSupplementEffectId(journal.getId(), req.supplementId(), req.effectId());
                SupplementEffect se = findOrCreateSupplementEffect(
                        req.supplementId(),
                        req.effectId()
                );
                Supplement s = supplementMap.get(id.getSupplementId());
                EffectTag t = effectMap.get(id.getEffectId());
                if (s == null || t == null) {
                    throw new IllegalArgumentException("Invalid supplement/effect ID");
                }
                JournalSupplementEffect newEffect = new JournalSupplementEffect(journal, s, t, req.size());
                newEffect.setScore();
                CalculateScore.addScore(se, newEffect);
                journalSupplementEffectRepo.save(newEffect);
            }
            return;
        }

        Map<JournalSupplementEffectId, JournalSupplementEffect> existingMap = journal.getJournalSupplementEffects().stream()
                .collect(Collectors.toMap(JournalSupplementEffect::getId, Function.identity()));

        Set<JournalSupplementEffectId> newIdSet = requests.stream()
                .map(req -> new JournalSupplementEffectId(journal.getId(), req.supplementId(), req.effectId()))
                .collect(Collectors.toSet());

        // 삭제
        Set<JournalSupplementEffectId> toDelete = new HashSet<>(existingMap.keySet());
        toDelete.removeAll(newIdSet);
        toDelete.forEach(id -> {
            JournalSupplementEffect jse = existingMap.get(id);
            SupplementEffect se = findOrCreateSupplementEffect(
                    jse.getSupplement().getId(),
                    jse.getEffect().getId()
            );
            if (!CalculateScore.deleteScore(se, jse, oldJournal)){
                supplementEffectRepo.deleteById(se.getId());
            }
            journal.getJournalSupplementEffects().remove(jse);
            journalSupplementEffectRepo.deleteById(id);
        });

        // 삽입 또는 갱신
        for (JSERequest req : requests) {
            JournalSupplementEffectId id = new JournalSupplementEffectId(journal.getId(), req.supplementId(), req.effectId());
            SupplementEffect se = findOrCreateSupplementEffect(
                    req.supplementId(),
                    req.effectId()
            );
            if (existingMap.containsKey(id)) {
                JournalSupplementEffect jse = existingMap.get(id);
                if (!CalculateScore.deleteScore(se, jse, oldJournal)){
                    supplementEffectRepo.deleteById(se.getId());
                }
                jse.setSize(req.size());
                jse.setScore();
                CalculateScore.addScore(se, jse);
            } else {
                Supplement s = supplementMap.get(req.supplementId());
                EffectTag t = effectMap.get(req.effectId());
                if (s == null || t == null) throw new IllegalArgumentException("Invalid supplement/effect ID");
                JournalSupplementEffect newEffect = new JournalSupplementEffect(journal, s, t, req.size());
                newEffect.setScore();
                CalculateScore.addScore(se, newEffect);
                journalSupplementEffectRepo.save(newEffect);
            }
        }
    }


    private void syncJSSE(Journal journal, Journal oldJournal, List<JSERequest> requests) {
        // 데이터 미리 로딩
        Map<Long, Supplement> supplementMap = supplementRepo.findAllById(
                        requests.stream().map(JSERequest::supplementId).toList())
                .stream().collect(Collectors.toMap(Supplement::getId, Function.identity()));

        Map<Long, SideEffectTag> effectMap = sideEffectRepo.findAllById(
                        requests.stream().map(JSERequest::effectId).toList())
                .stream().collect(Collectors.toMap(SideEffectTag::getId, Function.identity()));

        if (oldJournal == null) {
            for (JSERequest req : requests) {
                JournalSupplementSideEffectId id = new JournalSupplementSideEffectId(journal.getId(), req.supplementId(), req.effectId());
                SupplementSideEffect se = findOrCreateSupplementSideEffect(
                        req.supplementId(),
                        req.effectId()
                );
                Supplement s = supplementMap.get(id.getSupplementId());
                SideEffectTag t = effectMap.get(id.getEffectId());
                if (s == null || t == null) {
                    throw new IllegalArgumentException("Invalid supplement/sideEffect ID");
                }
                JournalSupplementSideEffect newEffect = new JournalSupplementSideEffect(journal, s, t, req.size());
                newEffect.setScore();
                CalculateScore.addScore(se, newEffect);
                journalSupplementSideEffectRepo.save(newEffect);
            }
            return;
        }

        Map<JournalSupplementSideEffectId, JournalSupplementSideEffect> existingMap = journal.getJournalSupplementSideEffects().stream()
                .collect(Collectors.toMap(JournalSupplementSideEffect::getId, Function.identity()));

        Set<JournalSupplementSideEffectId> newIdSet = requests.stream()
                .map(req -> new JournalSupplementSideEffectId(journal.getId(), req.supplementId(), req.effectId()))
                .collect(Collectors.toSet());

        // 삭제
        Set<JournalSupplementSideEffectId> toDelete = new HashSet<>(existingMap.keySet());
        toDelete.removeAll(newIdSet);
        toDelete.forEach(id -> {
            JournalSupplementSideEffect jse = existingMap.get(id);
            SupplementSideEffect se = findOrCreateSupplementSideEffect(
                    jse.getSupplement().getId(),
                    jse.getEffect().getId()
            );
            if (!CalculateScore.deleteScore(se, jse, oldJournal)){
                supplementSideEffectRepo.deleteById(se.getId());
            }
            journal.getJournalSupplementSideEffects().remove(jse);
            journalSupplementSideEffectRepo.deleteById(id);
        });


        // 삽입 또는 갱신
        for (JSERequest req : requests) {
            JournalSupplementSideEffectId id = new JournalSupplementSideEffectId(journal.getId(), req.supplementId(), req.effectId());
            SupplementSideEffect se = findOrCreateSupplementSideEffect(
                    req.supplementId(),
                    req.effectId()
            );
            if (existingMap.containsKey(id)) {
                JournalSupplementSideEffect jse = existingMap.get(id);
                if (!CalculateScore.deleteScore(se, jse, oldJournal)){
                    supplementSideEffectRepo.deleteById(se.getId());
                }
                jse.setSize(req.size());
                jse.setScore();
                CalculateScore.addScore(se, jse);
            } else {
                Supplement s = supplementMap.get(req.supplementId());
                SideEffectTag t = effectMap.get(req.effectId());
                if (s == null || t == null) throw new IllegalArgumentException("Invalid supplement/sideEffect ID");
                JournalSupplementSideEffect newEffect = new JournalSupplementSideEffect(journal, s, t, req.size());
                newEffect.setScore();
                CalculateScore.addScore(se, newEffect);
                journalSupplementSideEffectRepo.save(newEffect);
            }
        }
    }



    public List<JournalResponse> sort(Sort sort) {
        return journalRepo.findAll(sort)
                .stream()
                .map(JournalResponse::fromEntity)
                .toList();
    }

    public List<JournalResponse> search(String keyword, Sort sort) {
        return journalRepo.findByTitleContainingIgnoreCase(
                keyword, sort).
                stream()
                .map(JournalResponse::fromEntity)
                .toList();
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
        return journalRepo.findFiltered(trialDesign, blind, parallel, supplementIds, effectIds, sideEffectIds, sort)
                .stream()
                .map(JournalResponse::fromEntity)
                .toList();
    }

    public ResponseEntity<?> delete(Long id) {
        if (!journalRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        journalRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
