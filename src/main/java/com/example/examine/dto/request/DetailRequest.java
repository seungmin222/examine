package com.example.examine.dto.request;

public record DetailRequest(
        String intro,
        String positive,
        String negative,
        String mechanism,
        String dosage
) {}