package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.DetailRequest;
import com.example.examine.dto.JournalRequest;
import com.example.examine.dto.SupplementRequest;
import com.example.examine.dto.TagRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;

// 서비스
@Service
public class SupplementService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementDetailRepository supplementDetailRepo;
    private final JournalSupplementEffectRepository journalSupplementEffectRepo;
    private final JournalSupplementSideEffectRepository journalSupplementSideEffectRepo;

    public SupplementService(SupplementRepository supplementRepo,
                             TypeTagRepository typeRepo,
                             EffectTagRepository effectRepo,
                             SideEffectTagRepository sideEffectRepo,
                             SupplementDetailRepository supplementDetailRepo,
                             JournalSupplementEffectRepository journalSupplementEffectRepo,
                             JournalSupplementSideEffectRepository journalSupplementSideEffectRepo) {
        this.supplementRepo = supplementRepo;
        this.typeRepo = typeRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.supplementDetailRepo = supplementDetailRepo;
        this.journalSupplementEffectRepo = journalSupplementEffectRepo;
        this.journalSupplementSideEffectRepo = journalSupplementSideEffectRepo;
    }

    public ResponseEntity<?> create(SupplementRequest dto) {
        if (supplementRepo.findByKorNameAndEngName(dto.korName(), dto.engName()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 이름의 성분이 존재합니다.");
        }

        Supplement supplement = new Supplement();

        supplement.setKorName(dto.korName());
        supplement.setEngName(dto.engName());
        supplement.setDosage(dto.dosage());
        supplement.setCost(dto.cost());

        List<TypeTag> newTypes = dto.types() != null
                ? new ArrayList<>(typeRepo.findAllById(
                dto.types().stream().map(TagRequest::id).toList()))
                : new ArrayList<>();

        supplement.getTypes().addAll(newTypes);

        return ResponseEntity.ok(supplementRepo.save(supplement));
    }



    public ResponseEntity<?> update(Long id, SupplementRequest dto) {

        Optional<Supplement> opt = supplementRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Supplement supplement = opt.get();
        supplement.setKorName(dto.korName());
        supplement.setEngName(dto.engName());
        supplement.setDosage(dto.dosage());
        supplement.setCost(dto.cost());

        List<TypeTag> newTypes = dto.types() != null
                ? new ArrayList<>(typeRepo.findAllById(
                dto.types().stream().map(TagRequest::id).toList()))
                : new ArrayList<>();

        supplement.getTypes().clear(); // 기존 컬렉션 유지
        supplement.getTypes().addAll(newTypes); // 내부만 갱신

        Supplement updated = supplementRepo.save(supplement);
        return ResponseEntity.ok(updated);
    }

    public List<SupplementRequest> findAll(Sort sort){
        return supplementRepo.findAll(sort)
                .stream()
                .map(SupplementRequest::fromEntity)
                .toList();
    }

    public List<SupplementRequest> findOne(Long id) {
        return supplementRepo.findById(id)
                .stream()
                .map(SupplementRequest::fromEntity)
                .toList();
    }


    public List<SupplementRequest> search(String keyword, Sort sort){
        return supplementRepo.findByKorNameContainingIgnoreCaseOrEngNameContainingIgnoreCase(
                keyword, keyword, sort)
                .stream()
                .map(SupplementRequest::fromEntity)
                .toList();
    }

    public List<SupplementRequest> findFiltered(
            List<Long> typeIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            List<String> tiers,
            Sort sort
    ){
        return supplementRepo.findFiltered(typeIds, effectIds, sideEffectIds, tiers, sort)
                .stream()
                .map(SupplementRequest::fromEntity)
                .toList();
    }

    public List<JournalRequest> journals(Long id) {
        Set<Journal> journalSet = new HashSet<>();
        journalSupplementEffectRepo.findAllBySupplementId(id)
                .forEach(e -> journalSet.add(e.getJournal()));
        journalSupplementSideEffectRepo.findAllBySupplementId(id)
                .forEach(e -> journalSet.add(e.getJournal()));

        return journalSet.stream()
                .map(JournalRequest::fromEntity)
                .toList();
    }

    public ResponseEntity<?> delete(Long id) {
        if (!supplementRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        supplementRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public ResponseEntity<SupplementDetail> detail(@PathVariable Long id) {
        // Supplement 객체를 먼저 조회
        Optional<Supplement> supplementOpt = supplementRepo.findById(id);
        if (supplementOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // supplement를 기준으로 상세정보 조회
        Optional<SupplementDetail> detailOpt = supplementDetailRepo.findBySupplement(supplementOpt.get());
        return detailOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> detailUpdate(@PathVariable Long id, @RequestBody DetailRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", id);
        log.info("📥 받은 데이터: {}", dto);

        Optional<SupplementDetail> opt = supplementDetailRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        SupplementDetail s = opt.get();
        s.setIntro(dto.intro());
        s.setPositive(dto.positive());
        s.setNegative(dto.negative());
        s.setMechanism(dto.mechanism());
        s.setDosage(dto.dosage());

        SupplementDetail updated = supplementDetailRepo.save(s);
        return ResponseEntity.ok(updated);
    }
}
