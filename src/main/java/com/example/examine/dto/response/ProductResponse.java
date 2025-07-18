package com.example.examine.dto.response;

import com.example.examine.entity.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String link,
        String imageUrl,
        BigDecimal dosageValue,
        String dosageUnit,
        BigDecimal price,
        BigDecimal pricePerDose,
        Long brandId,
        String brandName,
        Long supplementId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getLink(),
                p.getImageUrl(),
                p.getDosageValue(),
                p.getDosageUnit(),
                p.getPrice(),
                p.getPricePerDose(),
                p.getBrand() != null ? p.getBrand().getId() : null,
                p.getBrand() != null ? p.getBrand().getName() : null,
                p.getSupplementDetail() != null ? p.getSupplementDetail().getSupplementId() : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
