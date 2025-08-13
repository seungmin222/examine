package com.example.examine.dto.response;

public record SearchSuggestResponse(
        Long id,
        String label,
        String type,
        Double score
) {
}
