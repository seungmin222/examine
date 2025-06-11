package com.example.examine.service;

import com.example.examine.dto.JournalRequest;
import com.example.examine.dto.SupplementRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

// 서비스
@Service
public class SupplementService {

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementDetailRepository supplementDetailRepo;

    public SupplementService(SupplementRepository supplementRepo,
                             TypeTagRepository typeRepo,
                             EffectTagRepository effectRepo,
                             SideEffectTagRepository sideEffectRepo,
                             SupplementDetailRepository supplementDetailRepo) {
        this.supplementRepo = supplementRepo;
        this.typeRepo = typeRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.supplementDetailRepo = supplementDetailRepo;
    }

    public ResponseEntity<?> create(SupplementRequest dto) {
        if (supplementRepo.findByKorNameAndEngName(dto.korName(), dto.engName()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 이름의 성분이 존재합니다.");
        }

        Supplement supplement = SupplementRequest.toEntity(dto, typeRepo);

        return ResponseEntity.ok(supplementRepo.save(supplement));
    }



    public ResponseEntity<?> update(Long id, SupplementRequest dto) {

        Optional<Supplement> opt = supplementRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Supplement supplement = opt.get();
        SupplementRequest.updateEntity(supplement, dto, typeRepo);

        Supplement updated = supplementRepo.save(supplement);
        return ResponseEntity.ok(updated);
    }

    public List<SupplementRequest> findAll(Sort sort){
        return supplementRepo.findAll(sort)
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
            Sort sort
    ){
        return supplementRepo.findFiltered(typeIds, effectIds, sideEffectIds, sort)
                .stream()
                .map(SupplementRequest::fromEntity)
                .toList();
    }

    public List<JournalRequest> journals(Long id){
        Optional<Supplement> opt = supplementRepo.findById(id);
        return opt.map(Supplement::getJournals).orElse(List.of())
                .stream()
                .map(JournalRequest::fromEntity)
                .toList();
    }

    public ResponseEntity<Supplement> findOne(@PathVariable Long id) {
        return supplementRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public ResponseEntity<?> delete(Long id) {
        if (!supplementRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        supplementRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

}
