package com.example.examine.dto.response;

import com.example.examine.entity.User.User;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        Integer level,
        List<PageResponse> pages,
        List<UserProductResponse> products
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getLevel(),
            user.getUserPages()
                    .stream()
                    .map(e->PageResponse.fromEntity(e.getPage()))
                    .toList(),
            user.getUserProducts()
                    .stream()
                    .map(UserProductResponse::fromEntity)
                    .toList()
        );
    }
}
