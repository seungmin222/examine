package com.example.examine.dto.request;

import com.example.examine.dto.response.DetailResponse;
import com.example.examine.dto.response.LLM.LLMJSE;
import com.example.examine.dto.response.LLM.LLMJSEId;
import com.example.examine.entity.Journal;
import com.example.examine.entity.detail.SupplementDetail;

import java.math.BigDecimal;

public record JSERequest(
        Long supplementId,
        Long effectId,
        BigDecimal cohenD,
        BigDecimal pearsonR,
        BigDecimal pValue
) {
    public static JSERequest fromEntity(LLMJSEId jseId, LLMJSE jse) {
        return new JSERequest(
                jseId.supplementId(),
                jseId.effectId(),
                jse.cohenD(),
                jse.pearsonR(),
                jse.pValue()
        );
    }
}