package com.example.examine.dto.response;

import com.example.examine.entity.Tag.Effect.Effect;
import com.example.examine.entity.Tag.Tag;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record TagTableResponse(
        Long id,
        String korName,
        String engName,
        List<TagResponse> supplements
) {
    public static TagTableResponse fromEntity(Tag tag, List<TagResponse> supplements) {

        return new TagTableResponse(
                tag.getId(),
                tag.getKorName(),
                tag.getEngName(),
                supplements
        );
    }
}
