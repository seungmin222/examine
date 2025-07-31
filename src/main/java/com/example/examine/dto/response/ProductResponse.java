package com.example.examine.dto.response;

import com.example.examine.entity.Product;
import com.example.examine.service.util.EnumService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String name,
        String link,
        String siteType,
        BigDecimal dosageValue,
        String dosageUnit,
        BigDecimal price,
        BigDecimal pricePerDose,
        TagResponse brand,
        Long supplementId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product p) {
        EnumService.ProductSiteType siteType = p.getSiteType();
        return new ProductResponse(
                p.getId(),
                p.getName(),
                siteType.buildUrl(p.getSiteProductId()),
                siteType.toString(),
                p.getDosageValue(),
                p.getDosageUnit().toString(),
                p.getPrice(),
                p.getPricePerDose(),
                TagResponse.fromEntity(p.getBrand()),
                p.getSupplementDetail() != null ? p.getSupplementDetail().getSupplementId() : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}
