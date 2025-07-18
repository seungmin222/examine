package com.example.examine.dto.request;

public record BrandRequest(
        String name,
        String country,
        String fei,
        Integer nai,
        Integer vai,
        Integer oai
) {}

