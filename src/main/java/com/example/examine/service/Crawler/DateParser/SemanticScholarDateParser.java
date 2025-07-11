package com.example.examine.service.Crawler.DateParser;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class SemanticScholarDateParser {
    public static LocalDate parse(String raw) {
        if (raw == null || raw.isBlank()) return null;

        try {
            // "2012 Apr 18:3:61." → "2012 Apr"
            String cleaned = raw.trim().split(" ")[0] + " " + raw.trim().split(" ")[1];
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy MMM", Locale.ENGLISH);
            return YearMonth.parse(cleaned, fmt).atDay(1);
        } catch (DateTimeParseException e) {
            System.err.println("❌ PubMed 날짜 파싱 실패: " + raw);
            return null;
        }
    }

}
