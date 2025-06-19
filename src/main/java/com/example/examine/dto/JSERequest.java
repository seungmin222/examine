package com.example.examine.dto;

import com.example.examine.entity.JournalSupplementEffect.JSE;

import java.math.BigDecimal;

public record JSERequest(
        Long supplementId,
        Long effectId,
        String supplementName,
        String effectName,
        BigDecimal size,
        BigDecimal score
) {
    public static JSERequest fromEntity(JSE jse) {
        return new JSERequest(
                jse.getId().getSupplementId(),
                jse.getId().getEffectId(),
                jse.getSupplement().getKorName(),
                jse.getEffect().getName(),
                jse.getSize(),
                jse.getScore()
        );
    }


}