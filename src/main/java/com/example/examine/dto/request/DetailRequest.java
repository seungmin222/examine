package com.example.examine.dto.request;

public record DetailRequest(
        Long id,
        String overview,
        String intro,
        String positive,
        String negative,
        String mechanism,
        String dosage
) {}