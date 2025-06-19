package com.example.examine.service.util;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculateScore {

    public static BigDecimal calculateJournalScore(Integer participants, Integer duration, String design, Integer blind) {
        if (participants == null){
            participants = 1;
        }
        if (duration == null){
            duration = 1;
        }
        if (blind == null){
            blind = 0;
        }
        BigDecimal logParticipants = BigDecimal.valueOf(Math.log10(participants + 1));
        BigDecimal logDuration = BigDecimal.valueOf(Math.log(duration + 1));
        BigDecimal designWeight = getDesignWeight(design, blind);

        return logParticipants
                .multiply(logDuration)
                .multiply(designWeight)
                .setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateJournalSupplementScore(BigDecimal strength, BigDecimal score ) {

        return strength
                .multiply(score)
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

    /// 점수 빼는 로직, 더하는 로직 나누기
    public static void deleteScore(SE se, JSE jse, Journal oldJournal) {
        int participants = oldJournal.getParticipants() != null ? oldJournal.getParticipants() : 1;
        se.setTotalScore(
                se.getTotalScore().subtract(
                        jse.getScore().multiply(BigDecimal.valueOf(participants))
                )
        );
        se.setTotalParticipants(se.getTotalParticipants()-participants);
        se.setFinalScore();
    }

    public static void addScore(SE se, JSE jse){
        int participants = jse.getJournal().getParticipants() != null ? jse.getJournal().getParticipants() : 1;
        se.setTotalScore(
                se.getTotalScore().add(
                        jse.getScore().multiply(BigDecimal.valueOf(participants))
                )
        );
        se.setTotalParticipants(se.getTotalParticipants()+participants);
        se.setFinalScore();
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
