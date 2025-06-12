package com.example.examine.dto;

import com.example.examine.entity.EffectTag;
import com.example.examine.entity.Supplement;
import com.example.examine.entity.SupplementEffect;

public record EffectRequest(
        Long id,
        String effectName,
        String tier
) {
    public static EffectRequest fromEntity(SupplementEffect effect) {
        return new EffectRequest(
                effect.getEffectTag().getId(),
                effect.getEffectTag().getName(),
                effect.getTier()
        );
    }
}