package com.example.examine.dto.request;

import com.example.examine.entity.JournalSupplementEffect.JSE;

import java.math.BigDecimal;

public record JSERequest(
        Long supplementId,
        Long effectId,
        BigDecimal size
) {

}