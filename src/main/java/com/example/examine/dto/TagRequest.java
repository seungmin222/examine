package com.example.examine.dto;

public record TagRequest(
        Long id,
        String name,
        String type
) {}