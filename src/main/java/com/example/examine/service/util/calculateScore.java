package com.example.examine.service.util;

import com.example.examine.entity.JournalSupplementEffect;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class calculateScore {

    public static BigDecimal calculateJournalScore(BigDecimal strength, int participants, int duration, String design, int blind) {
        BigDecimal logParticipants = BigDecimal.valueOf(Math.log10(participants + 1));
        BigDecimal logDuration = BigDecimal.valueOf(Math.log(duration + 1));
        BigDecimal designWeight = getDesignWeight(design, blind);

        return strength
                .multiply(logParticipants)
                .multiply(logDuration)
                .multiply(designWeight)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal getDesignWeight(String design, int blind) {
        BigDecimal baseWeight = switch (design.toLowerCase()) {
            case "meta" -> BigDecimal.valueOf(100);
            case "RCT" -> BigDecimal.valueOf(50);
            case "Non-RCT" -> BigDecimal.valueOf(30);
            case "cohort" -> BigDecimal.valueOf(10);
            default -> BigDecimal.ONE;
        };

        BigDecimal detailWeight = switch (blind) {
            case 2 -> BigDecimal.valueOf(1.5);
            case 1 -> BigDecimal.valueOf(1.3);
            default -> BigDecimal.valueOf(1.0);
        };

        return baseWeight.multiply(detailWeight);
    }
    public static BigDecimal calculateSupplementScore(List<JournalSupplementEffect> effects) {
        BigDecimal weightedSum = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.ZERO;

        for (JournalSupplementEffect e : effects) {
            BigDecimal score = e.getScore(); // 이미 strength × log × weight 계산됨
            int participants = e.getJournal().getParticipants();

            // 필터링 옵션
            if (participants < 10) continue;

            BigDecimal weight = BigDecimal.valueOf(participants);
            weightedSum = weightedSum.add(score.multiply(weight));
            weightSum = weightSum.add(weight);
        }

        BigDecimal finalScore = weightSum.compareTo(BigDecimal.ZERO) > 0
                ? weightedSum.divide(weightSum, 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return finalScore;
    }

    public static String calculateTier(BigDecimal score) {
        if (score == null) return "F";  // fallback
        if (score.compareTo(BigDecimal.valueOf(100)) >= 0) return "S";
        if (score.compareTo(BigDecimal.valueOf(50)) >= 0) return "A";
        if (score.compareTo(BigDecimal.valueOf(20)) >= 0) return "B";
        if (score.compareTo(BigDecimal.valueOf(10)) >= 0) return "C";
        return "D";
    }
}
