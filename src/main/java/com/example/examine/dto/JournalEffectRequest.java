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

    public static JournalSupplementEffect toEntity(
            JournalEffectRequest dto,
            Journal journal,
            SupplementRepository supplementRepo,
            EffectTagRepository effectRepo
    ) {
        Supplement supplement = supplementRepo.findById(dto.supplementId())
                .orElseThrow(() -> new IllegalArgumentException("해당 성분 없음: " + dto.supplementId()));
        EffectTag effectTag = effectRepo.findById(dto.effectId())
                .orElseThrow(() -> new IllegalArgumentException("해당 효과 없음: " + dto.effectId()));

        JournalSupplementEffect entity = new JournalSupplementEffect();
        entity.setJournal(journal);
        entity.setSupplement(supplement);
        entity.setEffectTag(effectTag);
        entity.setSize(dto.size());
        entity.setScore(); // 내부 계산 로직 있을 경우

        return entity;
    }

}