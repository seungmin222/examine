package com.example.examine.service.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeService {
    public static String relativeTime(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());

        if (duration.toMinutes() < 1) {
            return "방금 전";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + "분 전";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + "시간 전";
        } else if (duration.toDays() < 7) {
            return duration.toDays() + "일 전";
        } else {
            long weeks = duration.toDays() / 7;
            return weeks + "주 전";
        }
    }
}
