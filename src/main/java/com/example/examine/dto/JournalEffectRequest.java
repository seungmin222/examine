package com.example.examine.dto;

import com.example.examine.entity.EffectTag;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Supplement;
import com.example.examine.repository.*;
import com.example.examine.entity.JournalSupplementEffect;

import java.math.BigDecimal;

public record JournalEffectRequest(
        Long supplementId,
        Long effectId,
        String supplementName,
        String effectName,
        BigDecimal size,
        BigDecimal score
) {
    public static JournalEffectRequest fromEntity(JournalSupplementEffect e) {
        return new JournalEffectRequest(
                e.getSupplement().getId(),
                e.getEffectTag().getId(),
                e.getSupplement().getKorName(),
                e.getEffectTag().getName(),
                e.getSize(),
                e.getScore()
        );
    }


}