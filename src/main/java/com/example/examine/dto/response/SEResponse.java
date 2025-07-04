package com.example.examine.dto.response;

import com.example.examine.entity.SupplementEffect.SE;

import java.math.BigDecimal;

public record SEResponse(
        Long id,
        String korName,
        String engName,
        String tier,
        BigDecimal finalScore
) {
    public static SEResponse fromEntity(SE se) {
        return new SEResponse(
                se.getId().getEffectId(),
                se.getEffectKorName(),
                se.getEffectEngName(),
                se.getTier(),
                se.getFinalScore()
        );
    }
}
