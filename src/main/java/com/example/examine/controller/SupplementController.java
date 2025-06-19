package com.example.examine.controller;

import com.example.examine.dto.SupplementRequest;
import com.example.examine.repository.*;
import com.example.examine.service.EntityService.SupplementService;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/supplements")
public class SupplementController {

    private static final Logger log = LoggerFactory.getLogger(SupplementController.class);

    private final SupplementRepository supplementRepo;
    private final SupplementService supplementService;

    public SupplementController(SupplementRepository supplementRepo,
                                SupplementService supplementService) {
        this.supplementRepo = supplementRepo;
        this.supplementService = supplementService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SupplementRequest dto) {
       log.info("📥 받은 데이터: {}", dto);
       return supplementService.create(dto);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SupplementRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", id);
        log.info("📥 받은 데이터: {}", dto);
        return supplementService.update(id, dto);
    }


    @GetMapping
    public List<SupplementRequest> findAll(@RequestParam(defaultValue = "engName") String sort,
                                   @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findAll(sorting);
    }

    @GetMapping("/search")
    public List<SupplementRequest> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "engName") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.search(keyword, sorting);
    }

    @GetMapping("/filter")
    public List<SupplementRequest> filterByTagIds(
            @RequestParam(required = false) List<Long> typeIds,
            @RequestParam(required = false) List<Long> effectIds,
            @RequestParam(required = false) List<Long> sideEffectIds,
            @RequestParam(required = false) List<String> tiers,
            @RequestParam(defaultValue = "engName") String sort,       // ✅ 기본값 추가하면 더 안전
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findFiltered(typeIds, effectIds, sideEffectIds, tiers, sorting);
    }

    @GetMapping("/{id}")
    public List<SupplementRequest> findOne(@PathVariable Long id) {
        return supplementService.findOne(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return supplementService.delete(id);
    }

}
