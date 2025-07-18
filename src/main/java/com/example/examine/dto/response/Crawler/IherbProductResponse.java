package com.example.examine.dto.response.Crawler;

public record IherbProductResponse(
    String name,
    String imageUrl,
    String price,
    String pricePerDose
) {
}
