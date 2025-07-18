package com.example.examine.controller;

import com.example.examine.dto.response.Sale.IherbSaleResponse;
import com.example.examine.service.EntityService.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/iherb/refresh")
    public ResponseEntity<String> refresh() {
        return saleService.crawlIherbCoupons();
    }

    @GetMapping("/iherb")
    public List<IherbSaleResponse> getAll() {
        return saleService.getAllCoupons();
    }


}
