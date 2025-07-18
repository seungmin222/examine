package com.example.examine.dto.request;

import java.math.BigDecimal;

public record ProductRequest(
        Long supplementId,
        String link,
        String name,
        BigDecimal dosageValue,
        String dosageUnit,
        BigDecimal price,
        BigDecimal pricePerDose,
        Long brandId          // 브랜드 이름 대신 ID로 받는 게 일반적
) {}
