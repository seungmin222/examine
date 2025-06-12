package com.example.examine.service;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.JournalEffectRequest;
import com.example.examine.dto.JournalSideEffectRequest;
import com.example.examine.entity.*;
import com.example.examine.dto.JournalRequest;
import com.example.examine.repository.*;
import com.example.examine.service.crawler.ClinicalTrialsCrawler;
import com.example.examine.service.crawler.JournalMeta;
import com.example.examine.service.crawler.PubmedCrawler;
import com.example.examine.service.crawler.SemanticScholarCrawler;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static final String[] blindLabel = {
            "open-label", "single-blind", "double-blind"
    };




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

    public ResponseEntity<?> create(JournalRequest dto) {
        if (journalRepo.findByLink(dto.link()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 논문이 존재합니다.");
        }

        Journal journal = new Journal();
        journal.setDuration_value(dto.duration().value());
        journal.setDuration_unit(dto.duration().unit());
        journal.setDuration_days(dto.duration().days());
        journal.setParticipants(dto.participants());

        int blind = IntStream.range(0, blindLabel.length)
                .filter(i -> blindLabel[i].equals(dto.blind()))
                .findFirst()
                .orElse(0);
        journal.setBlind(blind);

        journal.setParallel(dto.parallel() == null || dto.parallel());

        if (dto.trialDesign() != null && dto.trialDesign().id() != null) {
            TrialDesign td = trialDesignRepo.findById(dto.trialDesign().id())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + dto.trialDesign().id()));
            journal.setTrial_design(td);
        } else {
            journal.setTrial_design(null);
        }

        syncJournalSupplementEffects(journal, dto.effects());
        syncJournalSupplementSideEffects(journal, dto.sideEffects());

        try {
            JournalMeta meta = crawlJournalMeta(dto.link());

            journal.setTitle(meta.getTitle());
            if(journal.getParticipants()==null){
              journal.setParticipants(meta.getParticipants());
            }
            journal.setSummary(meta.getSummary());
            journal.setDate(meta.getDate());
        } catch (IOException e) {
            System.out.println("크롤링 실패: " + e.getMessage());
        }

        return ResponseEntity.ok(journalRepo.save(journal));
    }

    public JournalMeta crawlJournalMeta(String url) throws IOException {
        if (url.contains("pubmed.ncbi.nlm.nih.gov")) {
            return pubmedCrawler.extract(url);
        } else if (url.contains("clinicaltrials.gov")) {
            return clinicalTrialsCrawler.extract(url);
        }
        else if (url.contains("semanticscholar")) {
            return semanticScholarCrawler.extract(url);
        } else {
            throw new IllegalArgumentException("지원하지 않는 논문 링크입니다.");
        }
    }

    @Transactional
    public ResponseEntity<?> update(Long id, JournalRequest dto) {
        Journal journal = journalRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Journal not found"));

        int blind = IntStream.range(0, blindLabel.length)
                .filter(i -> blindLabel[i].equals(dto.blind()))
                .findFirst()
                .orElse(0);
        int days = toDays(dto.duration().value(), dto.duration().unit());
        int oldDuration = journal.getDuration_days() != null ? journal.getDuration_days() : 0;
        int oldParticipants = journal.getParticipants() != null ? journal.getParticipants() : 0;

        Long oldTrialDesignId = journal.getTrial_design() != null ? journal.getTrial_design().getId() : null;
        Long newTrialDesignId = dto.trialDesign() != null ? dto.trialDesign().id() : null;

        TrialDesign newTrialDesign = null;
        if (dto.trialDesign() != null && newTrialDesignId != null) {
            newTrialDesign = trialDesignRepo.findById(newTrialDesignId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid trialDesign ID: " + newTrialDesignId));
            journal.setTrial_design(newTrialDesign);
        } else {
            journal.setTrial_design(null);
        }

        // ✅ 변경사항 비교 후 score 재계산
        boolean journalChanged =
                oldParticipants != dto.participants()
                        || oldDuration != days
                        || journal.getBlind() != blind
                        || !Objects.equals(oldTrialDesignId, newTrialDesignId);


        // 🔧 새 값 세팅
        journal.setDuration_value(dto.duration().value());
        journal.setDuration_unit(dto.duration().unit());
        journal.setDuration_days(days);
        journal.setBlind(blind);
        journal.setParallel(dto.parallel() == null || dto.parallel());
        journal.setParticipants(dto.participants());


        if (journalChanged) {
            List<JournalSupplementEffect> effects = journalSupplementEffectRepo.findAllByJournalId(id);
            List<JournalSupplementSideEffect> sideEffects = journalSupplementSideEffectRepo.findAllByJournalId(id);
            for (JournalSupplementEffect e : effects) {
                SupplementEffect agg = findOrCreateSupplementEffect(
                        e.getSupplement().getId(),
                        e.getEffectTag().getId()
                );
                e.setScore(agg, oldParticipants); // 내부에서 재계산, setScore() 내부에서 SupplementEffect에 영향 반영
            }
            for (JournalSupplementSideEffect e : sideEffects) {
                SupplementSideEffect agg = findOrCreateSupplementSideEffect(
                        e.getSupplement().getId(),
                        e.getSideEffectTag().getId()
                );
                e.setScore(agg, oldParticipants); // 내부에서 재계산, setScore() 내부에서 SupplementEffect에 영향 반영
            }
        }

        syncJournalSupplementEffects(journal, dto.effects());
        syncJournalSupplementSideEffects(journal, dto.sideEffects());

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
                            .orElseThrow(() -> new EntityNotFoundException("해당 sideEffectTag가 존재하지 않습니다."));
                    SupplementSideEffect newEntity = new SupplementSideEffect(supplement, sideEffect);
                    return supplementSideEffectRepo.save(newEntity);
                });
    }


    public static int toDays(int value, String unit) {
        return switch (unit.toLowerCase()) {
            case "day", "days" -> value;
            case "week", "weeks" -> value * 7;
            case "month", "months" -> value * 30;  // 평균 기준
            case "year", "years" -> value * 365;
            default -> throw new IllegalArgumentException("Invalid duration unit: " + unit);
        };
    }

    private void syncJournalSupplementEffects(Journal journal, List<JournalEffectRequest> requests) {
        Map<JournalSupplementEffectId, JournalSupplementEffect> existingMap = journal.getJournalSupplementEffects().stream()
                .collect(Collectors.toMap(JournalSupplementEffect::getId, Function.identity()));

        Set<JournalSupplementEffectId> newIdSet = requests.stream()
                .map(req -> new JournalSupplementEffectId(journal.getId(), req.supplementId(), req.effectId()))
                .collect(Collectors.toSet());

        // 삭제 대상
        Set<JournalSupplementEffectId> toDelete = new HashSet<>(existingMap.keySet());
        toDelete.removeAll(newIdSet);
        toDelete.forEach( id -> {
            JournalSupplementEffect entity = existingMap.get(id);
            journal.getJournalSupplementEffects().remove(entity); // 🔹 메모리 컬렉션에서 제거
            journalSupplementEffectRepo.deleteById(id);           // 🔥 실제 DB에서도 삭제
        });

        // 필요 데이터 미리 조회
        Map<Long, Supplement> supplementMap = supplementRepo.findAllById(
                        requests.stream().map(JournalEffectRequest::supplementId).toList())
                .stream().collect(Collectors.toMap(Supplement::getId, Function.identity()));

        Map<Long, EffectTag> effectMap = effectRepo.findAllById(
                        requests.stream().map(JournalEffectRequest::effectId).toList())
                .stream().collect(Collectors.toMap(EffectTag::getId, Function.identity()));

        // 삽입 또는 갱신
        for (JournalEffectRequest req : requests) {
            JournalSupplementEffectId id = new JournalSupplementEffectId(journal.getId(), req.supplementId(), req.effectId());
            SupplementEffect agg = findOrCreateSupplementEffect(
                    req.supplementId(),
                    req.effectId()
            );
            if (existingMap.containsKey(id)) {
                JournalSupplementEffect e = existingMap.get(id);
                e.setSize(req.size());
                e.setScore(agg, journal.getParticipants());
            } else {
                Supplement s = supplementMap.get(req.supplementId());
                EffectTag t = effectMap.get(req.effectId());
                if (s == null || t == null) throw new IllegalArgumentException("Invalid supplement/effect ID");
                JournalSupplementEffect newEffect = new JournalSupplementEffect(journal, s, t, req.size());
                newEffect.setScore(agg, journal.getParticipants());
                journalSupplementEffectRepo.save(newEffect);
            }
        }
    }

    private void syncJournalSupplementSideEffects(Journal journal, List<JournalSideEffectRequest> requests) {
        Map<JournalSupplementSideEffectId, JournalSupplementSideEffect> existingMap = journal.getJournalSupplementSideEffects().stream()
                .collect(Collectors.toMap(JournalSupplementSideEffect::getId, Function.identity()));

        Set<JournalSupplementSideEffectId> newIdSet = requests.stream()
                .map(req -> new JournalSupplementSideEffectId(journal.getId(), req.supplementId(), req.sideEffectId()))
                .collect(Collectors.toSet());

        Set<JournalSupplementSideEffectId> toDelete = new HashSet<>(existingMap.keySet());
        toDelete.removeAll(newIdSet);
        toDelete.forEach( id->{
            JournalSupplementSideEffect entity = existingMap.get(id);
            journal.getJournalSupplementSideEffects().remove(entity); // 🔹 메모리 컬렉션에서 제거
            journalSupplementSideEffectRepo.deleteById(id);           // 🔥 실제 DB에서도 삭제
        });

        Map<Long, Supplement> supplementMap = supplementRepo.findAllById(
                        requests.stream().map(JournalSideEffectRequest::supplementId).toList())
                    .stream().collect(Collectors.toMap(Supplement::getId, Function.identity()));

        Map<Long, SideEffectTag> effectMap = sideEffectRepo.findAllById(
                        requests.stream().map(JournalSideEffectRequest::sideEffectId).toList())
                .stream().collect(Collectors.toMap(SideEffectTag::getId, Function.identity()));

        for (JournalSideEffectRequest req : requests) {
            JournalSupplementSideEffectId id = new JournalSupplementSideEffectId(journal.getId(), req.supplementId(), req.sideEffectId());
            SupplementSideEffect agg = findOrCreateSupplementSideEffect(
                    req.supplementId(),
                    req.sideEffectId()
            );

            if (existingMap.containsKey(id)) {
                JournalSupplementSideEffect e = existingMap.get(id);
                e.setSize(req.size());
                e.setScore(agg, journal.getParticipants());
            } else {
                Supplement s = supplementMap.get(req.supplementId());
                SideEffectTag t = effectMap.get(req.sideEffectId());
                if (s == null || t == null) throw new IllegalArgumentException("Invalid supplement/sideEffect ID");
                JournalSupplementSideEffect newEffect = new JournalSupplementSideEffect(journal, s, t, req.size());
                newEffect.setScore(agg, journal.getParticipants());
                journalSupplementSideEffectRepo.save(newEffect);
            }
        }
    }


    public List<JournalRequest> sort(Sort sort) {
        return journalRepo.findAll(sort)
                .stream()
                .map(JournalRequest::fromEntity)
                .toList();
    }

    public List<JournalRequest> search(String keyword, Sort sort) {
        return journalRepo.findByTitleContainingIgnoreCase(
                keyword, sort).
                stream()
                .map(JournalRequest::fromEntity)
                .toList();
    }


    public List<JournalRequest> findFiltered(
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
                .map(JournalRequest::fromEntity)
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
