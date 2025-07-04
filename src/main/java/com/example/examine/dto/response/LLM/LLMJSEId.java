package com.example.examine.dto.response.LLM;

import java.math.BigDecimal;

public record LLMJSEId(
        Long supplementId,
        Long effectId,
        String effectType
) {
}