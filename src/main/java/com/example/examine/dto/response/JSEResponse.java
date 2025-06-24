package com.example.examine.dto.response;

import com.example.examine.dto.request.JSERequest;
import com.example.examine.entity.JournalSupplementEffect.JSE;

import java.math.BigDecimal;

public record JSEResponse(
        Long supplementId,
        Long effectId,
        String supplementName,
        String effectName,
        BigDecimal size,
        BigDecimal score
) {
    public static JSEResponse fromEntity(JSE jse) {
        return new JSEResponse(
                jse.getId().getSupplementId(),
                jse.getId().getEffectId(),
                jse.getSupplement().getKorName(),
                jse.getEffect().getName(),
                jse.getSize(),
                jse.getScore()
        );
    }
}
