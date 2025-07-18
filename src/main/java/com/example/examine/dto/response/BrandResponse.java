package com.example.examine.dto.response;

import com.example.examine.entity.Brand;
import java.time.LocalDateTime;

public record BrandResponse(
        Long id,
        String name,
        String country,
        String fei,
        int nai,
        int vai,
        int oai,
        double score,
        String tier,
        LocalDateTime createdAt
) {
    public static BrandResponse fromEntity(Brand brand) {
        return new BrandResponse(
                brand.getId(),
                brand.getName(),
                brand.getCountry(),
                brand.getFei(),
                brand.getNai(),
                brand.getVai(),
                brand.getOai(),
                brand.getScore(),
                brand.getTier(),
                brand.getCreatedAt()
        );
    }
}
