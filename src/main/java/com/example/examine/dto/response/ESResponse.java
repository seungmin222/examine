package com.example.examine.dto.response;

import com.example.examine.entity.SupplementEffect.SE;

import java.math.BigDecimal;

public record ESResponse(
        Long id,
        String korName,
        String engName,
        String tier,
        BigDecimal finalScore
) {
    public static ESResponse fromEntity(SE se) {
        return new ESResponse(
                se.getId().getSupplementId(),
                se.getSupplementKorName(),
                se.getSupplementEngName(),
                se.getTier(),
                se.getFinalScore()
        );
    }
}
