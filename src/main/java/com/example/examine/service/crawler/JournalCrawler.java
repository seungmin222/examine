package com.example.examine.service.crawler;

import java.io.IOException;

public interface JournalCrawler {
    JournalCrawlerMeta extract(String url) throws IOException;
}