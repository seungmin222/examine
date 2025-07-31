package com.example.examine.dto.request;

public record UserProductRequest(
        Long id,
        int quantity,
        boolean isChecked
) {
}
