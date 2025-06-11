package com.example.examine.dto;

import com.example.examine.entity.*;

public record TierTagRequest(
        Long id,
        String name,
        String tier
) {
    public static TierTagRequest fromEntity(Object entity) {
        if (entity instanceof TrialDesign t) {
            return new TierTagRequest(t.getId(), t.getName(), t.getTier());
        }
        else {
            throw new IllegalArgumentException("지원하지 않는 entity 타입: " + entity.getClass());
        }
    }
}
