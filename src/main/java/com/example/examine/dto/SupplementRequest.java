// SupplementRequest.java
package com.example.examine.dto;

import com.example.examine.entity.*;
import com.example.examine.repository.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public record SupplementRequest(
        Long id,// 프론트엔드에서 받은 문자열 변환
        String korName,
        String engName,
        String dosage,
        BigDecimal cost,
        List<TagRequest> types,
        List<EffectRequest> effects,
        List<SideEffectRequest> sideEffects
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
                        .map(e -> new EffectRequest(
                                e.getEffectTag().getId(), e.getEffectTag().getName(), e.getTier()))
                        .toList(),
                supplement.getSideEffects().stream()
                        .map(e -> new SideEffectRequest(
                                e.getSideEffectTag().getId(), e.getSideEffectTag().getName(), e.getTier()))
                        .toList()
        );
    }
    public static Supplement toEntity(SupplementRequest dto,
                                   TypeTagRepository typeTagRepo) {
        Supplement supplement = new Supplement();
        updateEntity(supplement, dto, typeTagRepo);
        return supplement;
    }

    public static void updateEntity(Supplement supplement,
                                    SupplementRequest dto,
                                    TypeTagRepository typeRepo) {

        supplement.setKorName(dto.korName());
        supplement.setEngName(dto.engName());
        supplement.setDosage(dto.dosage());
        supplement.setCost(dto.cost());

        // 🔹 1. Types
        List<TypeTag> newTypes = dto.types() != null
                ? new ArrayList<>(typeRepo.findAllById(
                dto.types().stream().map(TagRequest::id).toList()))
                : new ArrayList<>();

        supplement.getTypes().clear(); // 기존 컬렉션 유지
        supplement.getTypes().addAll(newTypes); // 내부만 갱신


    }

}
