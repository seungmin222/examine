package com.example.examine.dto.response;

import com.example.examine.dto.request.TagRequest;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.Effect.SideEffectTag;
import com.example.examine.entity.Supplement;
import com.example.examine.entity.TypeTag;

public record TagResponse(
        Long id,
        String name
) {
    public static TagResponse fromEntity(Long id, String name) {
        return new TagResponse(id, name);
    }
}
