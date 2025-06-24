package com.example.examine.dto.request;


import com.example.examine.entity.*;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.Effect.SideEffectTag;

public record TagRequest(
        Long id,
        String name,
        String type
) {
}