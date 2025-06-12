package com.example.examine.dto;

import com.example.examine.entity.*;

public record SideEffectRequest(
        Long id,
        String sideEffectName,
        String tier
) {
    public static SideEffectRequest fromEntity(SupplementSideEffect sideEffect) {
        return new SideEffectRequest(
                sideEffect.getSideEffectTag().getId(),
                sideEffect.getSideEffectTag().getName(),
                sideEffect.getTier()
        );
    }


}
