package com.example.examine.dto;

public record DetailRequest(
        Long supplement_id,
        String intro,
        String positive,
        String negative,
        String mechanism,
        String dosage
) {}