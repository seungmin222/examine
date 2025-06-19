package com.example.examine.dto;


import com.example.examine.entity.*;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.Effect.SideEffectTag;

public record TagRequest(
        Long id,
        String name,
        String type
) {
    public static TagRequest fromEntity(Object entity, String type) {
        if (entity instanceof Supplement s) {
            return new TagRequest(s.getId(), s.getKorName(), type);
        } else if (entity instanceof EffectTag e) {
            return new TagRequest(e.getId(), e.getName(), type);
        } else if (entity instanceof SideEffectTag se) {
            return new TagRequest(se.getId(), se.getName(), type);
        } else if (entity instanceof TypeTag t) {
            return new TagRequest(t.getId(), t.getName(), type);
        }  else {
            throw new IllegalArgumentException("지원하지 않는 entity 타입: " + entity.getClass());
        }
    }

}