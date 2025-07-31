package com.example.examine.service.Crawler.Parser;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IherbDateParser {

    private static final Pattern KST_PATTERN = Pattern.compile("한국시간\\s*(\\d+)월\\s*(\\d+)일\\s*(오전|오후)\\s*(\\d+)시");

    public static LocalDateTime parseKoreanTime(String raw) {
        Matcher matcher = KST_PATTERN.matcher(raw);
        if (!matcher.find()) return null;

        int month = Integer.parseInt(matcher.group(1));
        int day = Integer.parseInt(matcher.group(2));
        String ampm = matcher.group(3);
        int hour = Integer.parseInt(matcher.group(4));

        if (ampm.equals("오후") && hour < 12) hour += 12;
        if (ampm.equals("오전") && hour == 12) hour = 0;

        LocalDateTime now = LocalDateTime.now();
        int year = now.getYear();
        // 연말/연초 크로스 대비
        if (month < now.getMonthValue() - 6) year += 1;

        return LocalDateTime.of(year, month, day, hour, 0);
    }
}