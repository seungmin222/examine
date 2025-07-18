package com.example.examine.dto.response.Sale;

import com.example.examine.entity.Sale.IherbCoupon;

import java.time.LocalDateTime;

public record IherbSaleResponse(
        String detail,
        String promoCode,
        String link,
        LocalDateTime expiresAt
) {
    public static IherbSaleResponse fromEntity(IherbCoupon coupon) {
        return new IherbSaleResponse(
                coupon.getDetail(),
                coupon.getPromoCode(),
                coupon.getLink(),
                coupon.getExpiresAt()
        );
    }
}
