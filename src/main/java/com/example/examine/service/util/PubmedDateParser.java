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
            // "2012 Apr 18:3:61." → "2012 Apr"
            String[] sp = raw.trim().split(" ");
            String cleaned = raw.trim().split(" ")[0];
            if(sp.length > 1){
                cleaned += " " + sp[1];
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy MMM", Locale.ENGLISH);
            return YearMonth.parse(cleaned, fmt).atDay(1);
        } catch (DateTimeParseException e) {
            System.err.println("❌ PubMed 날짜 파싱 실패: " + raw);
            return null;
        }
    }

}
