package com.example.examine.service.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class PubmedDateParser {
    public static LocalDate parse(String raw) {
        if (raw == null || raw.isBlank()) return null;

        try {
            raw+="-01-01";
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // "2025-03-27" 같은 형식
            return LocalDate.parse(raw.trim(), fmt);
        } catch (DateTimeParseException e) {
            System.err.println("❌ PubMed 날짜 파싱 실패: " + raw);
            return null;
        }
    }

}
