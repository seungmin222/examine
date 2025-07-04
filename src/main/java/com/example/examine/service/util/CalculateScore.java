package com.example.examine.service.util;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.Tag.TrialDesign;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculateScore {

    public static BigDecimal calculateJournalScore(Integer participants, Integer duration, TrialDesign design, Integer blind) {
        if (participants == null || participants <= 0) {
            participants = 1;
        }
        if (duration == null ||  duration <= 0) {
            duration = 1;
        }
        if (blind == null || blind < 0){
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

    public static BigDecimal calculateJournalSupplementScore(
            BigDecimal sizeD,
            BigDecimal sizeR,
            BigDecimal p,
            BigDecimal score
    ) {
        BigDecimal weight = getPValueWeight(p);
        BigDecimal finalSize;

        if (sizeD != null && sizeR != null) {
            BigDecimal convertedRtoD = convertRtoD(sizeR);
            finalSize = sizeD.add(convertedRtoD)
                    .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        } else if (sizeD != null) {
            finalSize = sizeD;
        } else if (sizeR != null) {
            finalSize = convertRtoD(sizeR);
        } else {
            finalSize = BigDecimal.ONE;
        }

        return score
                .multiply(finalSize)
                .multiply(weight)
                .setScale(4, RoundingMode.HALF_UP);
    }

    private static BigDecimal convertRtoD(BigDecimal r) {
        if (r == null || r.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal numerator = r.multiply(BigDecimal.valueOf(2));
        BigDecimal denominator = BigDecimal.ONE.subtract(r.pow(2)); // 1 - r²

        // √(1 - r²)
        double sqrt = Math.sqrt(denominator.doubleValue());
        if (sqrt == 0) return BigDecimal.ZERO;

        return numerator.divide(BigDecimal.valueOf(sqrt), 4, RoundingMode.HALF_UP);
    }


    private static BigDecimal getPValueWeight(BigDecimal p) {
        if (p == null) return BigDecimal.valueOf(0.7); // 기본값 (보수적으로 처리)

        if (p.compareTo(new BigDecimal("0.001")) < 0) {
            return BigDecimal.valueOf(1.2);
        } else if (p.compareTo(new BigDecimal("0.01")) < 0) {
            return BigDecimal.valueOf(1.1);
        } else if (p.compareTo(new BigDecimal("0.05")) < 0) {
            return BigDecimal.valueOf(1.0);
        } else {
            return BigDecimal.valueOf(0.7); // 유의하지 않음
        }
    }


    private static BigDecimal getDesignWeight(TrialDesign td, int blind) {
        String design = "";
        if(td != null){
            design = td.getEngName();
        }
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
    public static boolean deleteScore(SE se, JSE jse) {
        int participants = (jse.getParticipants() != null && jse.getParticipants() > 0 ) ? jse.getParticipants() : 1;
        se.setTotalScore(
                se.getTotalScore().subtract(
                        jse.getScore().multiply(BigDecimal.valueOf(participants))
                )
        );
        se.setTotalParticipants(se.getTotalParticipants()-participants);
        if (se.getTotalParticipants()==0){
            return false;
        }
        else {
            se.setFinalScore();
            return true;
        }
    }

    public static void addScore(SE se, JSE jse){
        int participants = (jse.getParticipants() != null && jse.getParticipants() > 0 ) ? jse.getParticipants() : 1;
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
