package com.example.examine.service.crawler;

import java.io.IOException;

public interface JournalCrawler {
    JournalMeta extract(String url) throws IOException;
}