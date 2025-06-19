package com.example.examine.dto;

import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;

public record SERequest(
        Long id,
        String effectName,
        String tier
) {
    public static SERequest fromEntity(SE se) {
        return new SERequest(
                se.getId().getEffectId(),
                se.getEffect().getName(),
                se.getTier()
        );
    }
}