package com.example.examine.service.Crawler.JournalCrawler;

import com.example.examine.service.util.EnumService;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class JournalCrawlerMeta {
    private EnumService.JournalSiteType siteType;
    private String siteJournalId;
    private String title;
    private Integer participants;
    private String summary;
    private LocalDate date;

}