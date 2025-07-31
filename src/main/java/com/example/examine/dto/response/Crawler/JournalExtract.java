package com.example.examine.dto.response.Crawler;

import com.example.examine.service.util.EnumService;

public record JournalExtract(
        EnumService.JournalSiteType siteType,
        String siteJournalId
) {
}
