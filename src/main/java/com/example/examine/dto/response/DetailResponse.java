package com.example.examine.dto.response;

import com.example.examine.entity.SupplementDetail;

public record DetailResponse(
        Long supplement_id,
        String intro,
        String positive,
        String negative,
        String mechanism,
        String dosage
) {
    public static DetailResponse fromEntity(SupplementDetail d) {
        return new DetailResponse(
            d.getSupplement().getId(),
            d.getIntro(),
            d.getPositive(),
            d.getNegative(),
            d.getMechanism(),
            d.getDosage()
        );

    }
}
