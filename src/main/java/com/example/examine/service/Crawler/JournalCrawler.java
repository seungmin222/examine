package com.example.examine.service.Crawler;

import java.io.IOException;

public interface JournalCrawler {
    JournalCrawlerMeta extract(String url) throws IOException;
}