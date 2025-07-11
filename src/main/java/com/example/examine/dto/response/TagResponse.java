package com.example.examine.dto.response;

import com.example.examine.entity.Tag.Tag;

public record TagResponse(
        Long id,
        String korName,
        String engName,
        String tier
) {
    public static TagResponse fromEntity(Tag tag) {
        if (tag == null) {
            return null;
        }
        return new TagResponse(
                tag.getId(),
                tag.getKorName(),
                tag.getEngName(),
                tag.getTier()
        );
    }
}
