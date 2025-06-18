package com.example.examine.service.crawler;

import java.time.LocalDate;

public class JournalCrawlerMeta {
    private String title;
    private Integer participants;
    private String summary;
    private LocalDate date;

    public JournalCrawlerMeta(String title, Integer participants, String summary, LocalDate date) {
        this.title = title;
        this.participants = participants;
        this.summary = summary;
        this.date = date;
    }

    public String getSummary() { return summary; }
    public LocalDate getDate() { return date; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getParticipants() {
        return participants;
    }

    public void setParticipants(Integer participants) {
        this.participants = participants;
    }
}