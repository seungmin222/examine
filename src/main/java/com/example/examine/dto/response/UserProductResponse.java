package com.example.examine.dto.response;

import com.example.examine.entity.User.UserProduct;

import java.time.LocalDateTime;

public record UserProductResponse(
        ProductResponse product,
        int quantity,
        boolean isChecked,
        LocalDateTime updatedAt
){
    public static UserProductResponse fromEntity(UserProduct u){
        return new UserProductResponse(
                ProductResponse.fromEntity(u.getProduct()),
                u.getQuantity(),
                u.isChecked(),
                u.getUpdatedAt()
        );
    }
}
