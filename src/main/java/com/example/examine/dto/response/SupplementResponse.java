package com.example.examine.dto.response;

import com.example.examine.entity.Supplement;

import java.math.BigDecimal;
import java.util.List;

public record SupplementResponse(
        Long id,// 프론트엔드에서 받은 문자열 변환
        String korName,
        String engName,
        BigDecimal dosageValue,
        String dosageUnit,
        BigDecimal cost,
        List<TagResponse> types,
        List<SEResponse> effects,
        List<SEResponse> sideEffects
) {
    public static SupplementResponse fromEntity(Supplement supplement) {
        return new SupplementResponse(
                supplement.getId(),
                supplement.getKorName(),
                supplement.getEngName(),
                supplement.getDosageValue(),
                supplement.getDosageUnit(),
                supplement.getCost(),
                supplement.getTypes().stream()
                        .map(e->new TagResponse(e.getId(),e.getName()))
                        .toList(),
                supplement.getEffects().stream()
                        .map(e -> new SEResponse(
                                e.getEffect().getId(), e.getEffect().getName(), e.getTier()))
                        .toList(),
                supplement.getSideEffects().stream()
                        .map(e -> new SEResponse(
                                e.getEffect().getId(), e.getEffect().getName(), e.getTier()))
                        .toList()
        );
    }
}
