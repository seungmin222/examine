package com.example.examine.dto;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementSideEffect;
import com.example.examine.entity.SideEffectTag;
import com.example.examine.entity.Supplement;
import com.example.examine.repository.SideEffectTagRepository;
import com.example.examine.repository.SupplementRepository;

import java.math.BigDecimal;

public record JournalSideEffectRequest(
        Long supplementId,
        Long sideEffectId,
        String supplementName,
        String sideEffectName,
        BigDecimal size,
        BigDecimal score
) {
    public static JournalSideEffectRequest fromEntity(JournalSupplementSideEffect e) {
        return new JournalSideEffectRequest(
                e.getSupplement().getId(),
                e.getSideEffectTag().getId(),
                e.getSupplement().getKorName(),
                e.getSideEffectTag().getName(),
                e.getSize(),
                e.getScore()
        );
    }

    public static JournalSupplementSideEffect toEntity(
            JournalSideEffectRequest dto,
            Journal journal,
            SupplementRepository supplementRepo,
            SideEffectTagRepository sideEffectRepo
    ) {
        Supplement supplement = supplementRepo.findById(dto.supplementId())
                .orElseThrow(() -> new IllegalArgumentException("해당 성분 없음: " + dto.supplementId()));
        SideEffectTag sideEffectTag = sideEffectRepo.findById(dto.sideEffectId())
                .orElseThrow(() -> new IllegalArgumentException("해당 부작용 없음: " + dto.sideEffectId()));

        JournalSupplementSideEffect entity = new JournalSupplementSideEffect();
        entity.setJournal(journal);
        entity.setSupplement(supplement);
        entity.setSideEffectTag(sideEffectTag);
        entity.setSize(dto.size());

        return entity;
    }

}