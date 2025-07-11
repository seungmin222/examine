package com.example.examine.dto.response;

import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.SupplementEffect.SE;

import java.math.BigDecimal;


public record JSEResponse(
        Long journalId,
        Long supplementId,
        Long effectId,
        String supplementKorName,
        String supplementEngName,
        String effectKorName,
        String effectEngName,
        BigDecimal cohenD,
        BigDecimal pearsonR,
        BigDecimal pValue,
        BigDecimal score
) {
    public static JSEResponse fromEntity(JSE jse) {
        return new JSEResponse(
                jse.getId().getJournalId(),
                jse.getId().getSEId().getSupplementId(),
                jse.getId().getSEId().getEffectId(),
                jse.getSE().getSupplementKorName(),
                jse.getSE().getSupplementEngName(),
                jse.getSE().getEffectKorName(),
                jse.getSE().getEffectEngName(),
                jse.getCohenD(),
                jse.getPearsonR(),
                jse.getPValue(),
                jse.getScore()
        );
    }
}
