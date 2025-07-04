package com.example.examine.dto.response;

import com.example.examine.entity.Tag.Supplement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
        List<SEResponse> effects = new ArrayList<>(
                supplement.getEffects().stream()
                        .map(SEResponse::fromEntity)
                        .toList()
        );
        effects.sort(Comparator.comparing(SEResponse::finalScore).reversed());

        List<SEResponse> sideEffects = new ArrayList<>(
                supplement.getSideEffects().stream()
                        .map(SEResponse::fromEntity)
                        .toList()
        );
        sideEffects.sort(Comparator.comparing(SEResponse::finalScore).reversed());

        return new SupplementResponse(

                supplement.getId(),
                supplement.getKorName(),
                supplement.getEngName(),
                supplement.getDosageValue(),
                supplement.getDosageUnit(),
                supplement.getCost(),
                supplement.getTypes().stream()
                        .map(TagResponse::fromEntity)
                        .toList(),
                effects,
                sideEffects
        );
    }
}
