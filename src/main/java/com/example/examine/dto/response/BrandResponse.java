package com.example.examine.dto.response;

import com.example.examine.entity.Tag.Brand;
import java.time.LocalDateTime;

public record BrandResponse(
        Long id,
        String korName,
        String engName,
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
                brand.getKorName(),
                brand.getEngName(),
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
