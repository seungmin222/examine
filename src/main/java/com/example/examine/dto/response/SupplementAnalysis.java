package com.example.examine.dto.response;

import java.math.BigDecimal;

public record SupplementAnalysis (
        String korName,
        String engName,
        BigDecimal dosageValue,
        String dosageUnit
){
}
