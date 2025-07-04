package com.example.examine.dto.response;

import com.example.examine.entity.detail.SupplementDetail;

public record DetailResponse(
        String intro,
        String positive,
        String negative,
        String mechanism,
        String dosage
) {
    public static DetailResponse fromEntity(SupplementDetail d) {
        return new DetailResponse(
            d.getIntro(),
            d.getPositive(),
            d.getNegative(),
            d.getMechanism(),
            d.getDosage()
        );
    }
}
