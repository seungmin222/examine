package com.example.examine.dto.response;

import com.example.examine.entity.detail.Detail;

public record TagDetailResponse(
        String overview,
        String intro
) {
    public static TagDetailResponse fromEntity(Detail tagDetail) {
         return new TagDetailResponse(
             tagDetail.getOverview(),
             tagDetail.getIntro()
         );
    }
}
