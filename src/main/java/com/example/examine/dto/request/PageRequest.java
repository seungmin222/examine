package com.example.examine.dto.request;

public record PageRequest(
        String link,
        String title,
        int level
) {
}
