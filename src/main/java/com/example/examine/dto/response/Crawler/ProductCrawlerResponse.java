package com.example.examine.dto.response.Crawler;

import com.example.examine.service.util.EnumService;

import java.math.BigDecimal;

public record ProductCrawlerResponse(
    EnumService.ProductSiteType siteType,
    String siteProductId,
    String name,
    BigDecimal price,
    BigDecimal pricePerDose,
    String brandName
) {
}
