package com.example.examine.controller;

import com.example.examine.dto.SupplementRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import com.example.examine.service.*;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/supplements")
public class SupplementRestController {

    private static final Logger log = LoggerFactory.getLogger(SupplementRestController.class);

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementDetailRepository supplementDetailRepo;
    private final SupplementService supplementService;

    public SupplementRestController(SupplementRepository supplementRepo,
                                    TypeTagRepository typeRepo,
                                    EffectTagRepository effectRepo,
                                    SideEffectTagRepository sideEffectRepo,
                                    SupplementSideEffectRepository supplementSideEffectRepo,
                                    SupplementEffectRepository supplementEffectRepo,
                                    SupplementDetailRepository supplementDetailRepo,
                                    SupplementService supplementService) {
        this.supplementRepo = supplementRepo;
        this.typeRepo = typeRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.supplementSideEffectRepo = supplementSideEffectRepo;
        this.supplementEffectRepo = supplementEffectRepo;
        this.supplementDetailRepo = supplementDetailRepo;
        this.supplementService = supplementService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SupplementRequest dto) {
        if (supplementRepo.findByKorNameAndEngName(dto.korName(), dto.engName()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 이름의 성분이 존재합니다.");
        }

        Supplement s = new Supplement();
        s.setKorName(dto.korName());
        s.setEngName(dto.engName());
        s.setDosage(dto.dosage());
        s.setCost(dto.cost());

        if (dto.typeIds() != null) {
            s.setTypes(typeRepo.findAllById(dto.typeIds()));
        }

        if (dto.effectGrades() != null) {
            List<SupplementEffect> newMappings = dto.effectGrades().stream()
                    .map(e -> new SupplementEffect(
                            s,
                            effectRepo.findById(e.effectId()).orElseThrow(),
                            e.grade()
                    ))
                    .toList();
            s.setEffects(newMappings);
        }

        if (dto.sideEffectGrades() != null) {
            List<SupplementSideEffect> newMappings = dto.sideEffectGrades().stream()
                    .map(e -> new SupplementSideEffect(
                            s,
                            sideEffectRepo.findById(e.sideEffectId()).orElseThrow(),
                            e.grade()
                    ))
                    .toList();
            s.setSideEffects(newMappings);
        }

        Supplement saved = supplementRepo.save(s);
        SupplementDetail detail = new SupplementDetail();
        detail.setIntro("");
        detail.setPositive("");
        detail.setNegative("");
        detail.setMechanism("");
        detail.setDosage("");
        detail.setSupplement(saved); // 🔁 supplement 참조 설정

        supplementDetailRepo.save(detail);
        return ResponseEntity.ok(saved);
    }
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplementRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", id);
        log.info("📥 받은 데이터: {}", dto);

        Optional<Supplement> opt = supplementRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Supplement s = opt.get();
        s.setKorName(dto.korName());
        s.setEngName(dto.engName());
        s.setDosage(dto.dosage());
        s.setCost(dto.cost());

        s.setTypes(dto.typeIds() != null
                ? typeRepo.findAllById(dto.typeIds())
                : List.of());

        supplementEffectRepo.deleteBySupplementId(id);

        if (dto.effectGrades() != null) {
            List<SupplementEffect> newMappings = dto.effectGrades().stream()
                    .map(e -> new SupplementEffect(
                            s,
                            effectRepo.findById(e.effectId()).orElseThrow(),
                            e.grade()
                    ))
                    .toList();
            s.getEffects().clear();
            s.getEffects().addAll(newMappings);  // ✅ 이 부분!
        } else {
            s.getEffects().clear();  // 🔄 null이 올 경우도 안전하게 처리
        }

        supplementSideEffectRepo.deleteBySupplementId(id);

        if (dto.sideEffectGrades() != null) {
            List<SupplementSideEffect> newMappings = dto.sideEffectGrades().stream()
                    .map(e -> new SupplementSideEffect(
                            s,
                            sideEffectRepo.findById(e.sideEffectId()).orElseThrow(),
                            e.grade()
                    ))
                    .toList();
            s.getSideEffects().clear();
            s.getSideEffects().addAll(newMappings);  // ✅ 이 부분!
        } else {
            s.getSideEffects().clear();  // 🔄 null이 올 경우도 안전하게 처리
        }


        Supplement updated = supplementRepo.save(s);
        return ResponseEntity.ok(updated);
    }


    @GetMapping
    public List<Supplement> sortBy(@RequestParam(defaultValue = "engName") String sort,
                                   @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementRepo.findAll(sorting);
    }

    @GetMapping("/search")
    public List<Supplement> searchAndSort(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "engName") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementRepo.findByKorNameContainingIgnoreCaseOrEngNameContainingIgnoreCase(
                keyword, keyword, sorting
        );
    }

    @GetMapping("/filter")
    public List<Supplement> filterByTagIds(
            @RequestParam(required = false) List<Long> typeIds,
            @RequestParam(required = false) List<Long> effectIds,
            @RequestParam(required = false) List<Long> sideEffectIds,
            @RequestParam(defaultValue = "engName") String sort,       // ✅ 기본값 추가하면 더 안전
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findFiltered(typeIds, effectIds, sideEffectIds, sorting);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!supplementRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        supplementRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/pubmeds")
    public List<Pubmed> getPubmedsForSupplement(@PathVariable Long id) {
        Optional<Supplement> opt = supplementRepo.findById(id);
        return opt.map(Supplement::getPubmeds).orElse(List.of());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplement> findOne(@PathVariable Long id) {
        return supplementRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
