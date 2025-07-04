package com.example.examine.dto.response;

import com.example.examine.entity.detail.TagDetail;

public record TagDetailResponse(
        String overview,
        String intro
) {
    public static TagDetailResponse fromEntity(TagDetail tagDetail) {
         return new TagDetailResponse(
             tagDetail.getOverview(),
             tagDetail.getIntro()
         );
    }
}
