package com.example.examine.dto.request;

public record TextSimilarityRequest(
        Long id,
        String name,
        String type,
        Double score
) {
}
