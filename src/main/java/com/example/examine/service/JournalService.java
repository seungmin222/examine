package com.example.examine.service;

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

    private final JournalRepository journalRepo;
    private final TrialDesignRepository trialDesignRepo;
    private final SupplementRepository supplementRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
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

        Journal journal = JournalRequest.toEntity(dto, trialDesignRepo, supplementRepo, effectRepo, sideEffectRepo, journalSupplementEffectRepo, journalSupplementSideEffectRepo);

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

        return ResponseEntity.ok(journalRepo.save(journal));
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

            if (existingMap.containsKey(id)) {
                JournalSupplementEffect e = existingMap.get(id);
                e.setSize(req.size());
            } else {
                Supplement s = supplementMap.get(req.supplementId());
                EffectTag t = effectMap.get(req.effectId());
                if (s == null || t == null) throw new IllegalArgumentException("Invalid supplement/effect ID");
                JournalSupplementEffect newEffect = new JournalSupplementEffect(journal, s, t, req.size());
                newEffect.setScore();
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

            if (existingMap.containsKey(id)) {
                JournalSupplementSideEffect e = existingMap.get(id);
                e.setSize(req.size());
            } else {
                Supplement s = supplementMap.get(req.supplementId());
                SideEffectTag t = effectMap.get(req.sideEffectId());
                if (s == null || t == null) throw new IllegalArgumentException("Invalid supplement/sideEffect ID");
                JournalSupplementSideEffect newEffect = new JournalSupplementSideEffect(journal, s, t, req.size());
                newEffect.setScore();
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
