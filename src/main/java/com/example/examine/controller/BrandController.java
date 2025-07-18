package com.example.examine.controller;

import com.example.examine.dto.request.BrandRequest;
import com.example.examine.dto.response.BrandResponse;
import com.example.examine.entity.Brand;
import com.example.examine.service.EntityService.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/brand")
public class BrandController {

    private final BrandService brandService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<Brand> createBrand(@RequestBody BrandRequest request) {
        Brand saved = brandService.create(request);
        return ResponseEntity.ok(saved);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Brand> updateBrand(@PathVariable Long id, @RequestBody BrandRequest request) {
        Brand updated = brandService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public List<BrandResponse> getAllBrands(@RequestParam(defaultValue = "name") String sort,
                                            @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return brandService.findAllSorted(sorting);
    }

    @GetMapping("/search")
    public List<BrandResponse> searchBrands(@RequestParam String keyword,
                                            @RequestParam(defaultValue = "name") String sort,
                                            @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return brandService.searchByKeyword(keyword, sorting);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

