package com.example.examine.service.Crawler.JournalCrawler;

import java.io.IOException;

public interface JournalCrawler {
    JournalCrawlerMeta extract(String url) throws IOException;
}