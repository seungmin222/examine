package com.example.examine.dto.response;

import com.example.examine.dto.request.SERequest;
import com.example.examine.entity.SupplementEffect.SE;

public record SEResponse(
        Long id,
        String effectName,
        String tier
) {
    public static SEResponse fromEntity(SE se) {
        return new SEResponse(
                se.getId().getEffectId(),
                se.getEffect().getName(),
                se.getTier()
        );
    }
}
