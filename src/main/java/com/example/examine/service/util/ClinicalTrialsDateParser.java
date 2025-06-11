package com.example.examine.service.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ClinicalTrialsDateParser {
    public static LocalDate parse(String raw) {
        if (raw == null || raw.isBlank()) return null; // ✅ 여기가 반드시 필요함

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // "2025-03-27" 같은 형식
            return LocalDate.parse(raw.trim(), fmt);
        } catch (DateTimeParseException e) {
            System.err.println("❌ ClinicalTrials 날짜 파싱 실패: " + raw);
            return null;
        }
    }

}
