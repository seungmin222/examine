// SupplementRequest.java
package com.example.examine.dto;

import com.example.examine.entity.*;
import java.math.BigDecimal;
import java.util.List;

public record SupplementRequest(
        Long id,// 프론트엔드에서 받은 문자열 변환
        String korName,
        String engName,
        String dosage,
        BigDecimal cost,
        List<TagRequest> types,
        List<SERequest> effects,
        List<SERequest> sideEffects
) {
    public static SupplementRequest fromEntity(Supplement supplement) {
        return new SupplementRequest(
                supplement.getId(),
                supplement.getKorName(),
                supplement.getEngName(),
                supplement.getDosage(),
                supplement.getCost(),
                supplement.getTypes().stream()
                        .map(e->new TagRequest(e.getId(),e.getName(),"type"))
                        .toList(),
                supplement.getEffects().stream()
                        .map(e -> new SERequest(
                                e.getEffect().getId(), e.getEffect().getName(), e.getTier()))
                        .toList(),
                supplement.getSideEffects().stream()
                        .map(e -> new SERequest(
                                e.getEffect().getId(), e.getEffect().getName(), e.getTier()))
                        .toList()
        );
    }
}
