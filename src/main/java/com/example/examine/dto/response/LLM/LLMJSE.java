package com.example.examine.dto.response.LLM;

import java.math.BigDecimal;

public record LLMJSE(
        String supplement,
        String effect,
        BigDecimal cohenD,
        BigDecimal pearsonR,
        BigDecimal pValue
) {
}
