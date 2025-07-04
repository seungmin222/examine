package com.example.examine.dto.response;

import com.example.examine.entity.Tag.Effect.Effect;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record EffectTableResponse (
      Long id,
      String korName,
      String engName,
      List<ESResponse> supplements
) {
    public static EffectTableResponse fromEntity(Effect effect) {
        List<ESResponse> supplements = new ArrayList<> (effect.getSE().stream()
                .map(ESResponse::fromEntity)
                .toList()
        );

        supplements.sort(Comparator.comparing(ESResponse::finalScore).reversed());

        return new EffectTableResponse(
                effect.getId(),
                effect.getKorName(),
                effect.getEngName(),
                supplements
        );
    }
}
