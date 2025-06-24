package com.example.examine.dto.response;

import com.example.examine.entity.Journal;
import com.example.examine.entity.User;

public record UserResponse(
        String username,
        Integer level
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getUsername(),
            user.getLevel()
        );
    }
}
