package com.example.examine.dto.response;

import com.example.examine.entity.JournalSupplementEffect.JSE;

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
}
