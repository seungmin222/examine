package com.example.examine.controller;

import com.example.examine.dto.request.DetailRequest;
import com.example.examine.dto.request.ProductRequest;
import com.example.examine.dto.request.SupplementRequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.dto.response.ProductResponse;
import com.example.examine.dto.response.SupplementDetailResponse;
import com.example.examine.dto.response.SupplementResponse;
import com.example.examine.repository.TagRepository.SupplementRepository;
import com.example.examine.service.EntityService.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/supplements")
@RequiredArgsConstructor
public class SupplementController {

    private static final Logger log = LoggerFactory.getLogger(SupplementController.class);

    private final SupplementRepository supplementRepo;
    private final SupplementService supplementService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<String> create(@RequestBody SupplementRequest dto) {
       log.info("📥 받은 데이터: {}", dto);
       return supplementService.create(dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody SupplementRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", id);
        log.info("📥 받은 데이터: {}", dto);
        return supplementService.update(id, dto);
    }


    @GetMapping
    public List<SupplementResponse> findAll(@RequestParam(defaultValue = "engName") String sort,
                                            @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findAll(sorting);
    }

    @GetMapping("/search")
    public List<SupplementResponse> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "engName") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.search(keyword, sorting);
    }

    @GetMapping("/filter")
    public List<SupplementResponse> filterByTagIds(
            @RequestParam(required = false) List<Long> type,
            @RequestParam(required = false) List<Long> effect,
            @RequestParam(required = false) List<Long> sideEffect,
            @RequestParam(required = false) List<String> tier,
            @RequestParam(defaultValue = "engName") String sort,       // ✅ 기본값 추가하면 더 안전
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findFiltered(type, effect, sideEffect, tier, sorting);
    }

    @PutMapping("/detail")
    public ResponseEntity<String> update(@RequestBody DetailRequest dto) {
        return supplementService.detailUpdate(dto);
    }

    @GetMapping("/detail/{id}")
    public SupplementDetailResponse findDetail(@PathVariable Long id,@RequestParam(defaultValue = "engName") String sort,
                                                     @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findDetail(id, sorting);
    }

    @GetMapping("/detail/{id}/journals")
    public List<JournalResponse> findJournals(@PathVariable Long id,
                                            @RequestParam(defaultValue = "engName") String sort,
                                            @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.findJournals(id, sorting);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return supplementService.delete(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/detail/products")
    public ResponseEntity<String> createProduct(@RequestBody ProductRequest dto) {
        return supplementService.createProduct(dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/detail/products/{productId}")
    public ResponseEntity<String> updateProduct(@PathVariable Long productId,
                                                @RequestBody ProductRequest dto) {
        return supplementService.updateProduct(productId, dto);
    }

    @GetMapping("/detail/{id}/products")
    public List<ProductResponse> getProducts(@PathVariable Long id,
                                             @RequestParam(defaultValue = "name") String sort,
                                             @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.getProducts(id, sorting);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/detail/{id}/products")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        return supplementService.deleteProduct(id);
    }
}
