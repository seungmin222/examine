package com.example.examine.dto.request;


public record TagRequest(
        String korName,
        String engName,
        String type
) {
}