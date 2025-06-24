package com.example.examine.dto.response;

import com.example.examine.dto.request.TierTagRequest;
import com.example.examine.entity.TrialDesign;

public record TierTagResponse(
        Long id,
        String name,
        String tier
) {
    public static TierTagResponse fromEntity(Object entity) {
        if (entity instanceof TrialDesign t) {
            return new TierTagResponse(t.getId(), t.getName(), t.getTier());
        }
        else {
            throw new IllegalArgumentException("지원하지 않는 entity 타입: " + entity.getClass());
        }
    }
}
