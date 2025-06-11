package com.example.examine.controller;

import com.example.examine.dto.DetailRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/details") // api 완전분리해서 자원낭비 최소화
public class DetailController {

    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final SupplementDetailRepository supplementdetailRepo;
    private final SupplementRepository supplementRepo;

    public DetailController(SupplementRepository supplementRepo,
            SupplementDetailRepository supplementdetailRepo) {
        this.supplementRepo = supplementRepo;
        this.supplementdetailRepo = supplementdetailRepo;
    }

    @PutMapping ("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DetailRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", id);
        log.info("📥 받은 데이터: {}", dto);

        Optional<SupplementDetail> opt = supplementdetailRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        SupplementDetail s = opt.get();
        s.setIntro(dto.intro());
        s.setPositive(dto.positive());
        s.setNegative(dto.negative());
        s.setMechanism(dto.mechanism());
        s.setDosage(dto.dosage());

        SupplementDetail updated = supplementdetailRepo.save(s);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplementDetail> findOne(@PathVariable Long id) {
        // Supplement 객체를 먼저 조회
        Optional<Supplement> supplementOpt = supplementRepo.findById(id);
        if (supplementOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // supplement를 기준으로 상세정보 조회
        Optional<SupplementDetail> detailOpt = supplementdetailRepo.findBySupplement(supplementOpt.get());
        return detailOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/journals")
    public List<Journal> getJournals(@PathVariable Long id) {
        Optional<Supplement> opt = supplementRepo.findById(id);
        return opt.map(Supplement::getJournals).orElse(List.of());
    }

}
