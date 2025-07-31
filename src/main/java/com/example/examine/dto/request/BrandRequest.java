package com.example.examine.dto.request;

public record BrandRequest(
        String korName,
        String engName,
        String country,
        String fei,
        Integer nai,
        Integer vai,
        Integer oai
) {}

