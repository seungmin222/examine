package com.example.examine.dto.response;

public record DurationResponse(
        Integer value,
        String unit,
        Integer days
) {
}
