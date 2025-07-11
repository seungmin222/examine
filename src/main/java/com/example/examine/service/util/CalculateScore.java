package com.example.examine.service.util;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.service.EntityService.JournalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculateScore {
    private static final Logger log = LoggerFactory.getLogger(CalculateScore.class);

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

        if (sizeD != null) {
            finalSize = sizeD;
        } else if (sizeR != null) {
            finalSize = convertRtoD(sizeR);
        } else {
            finalSize = BigDecimal.ZERO;
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
            case "rct" -> BigDecimal.valueOf(50);
            case "non-rct" -> BigDecimal.valueOf(30);
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
    public static void addScore(SE se, JSE jse){
        int participants = (jse.getParticipants() != null && jse.getParticipants() > 0 ) ? jse.getParticipants() : 1;
        BigDecimal score = jse.getScore();

        log.info("📊 [addScore] SE: {}, participants: {}, score: {}", se.getId(), participants, score); // 🔥 추가
        // 총합 점수/참가자 수 반영
        se.setTotalScore(
                se.getTotalScore().add(score.multiply(BigDecimal.valueOf(participants)))
        );
        se.setTotalParticipants(se.getTotalParticipants() + participants);

        Direction direction = countDirection(jse.getCohenD(), jse.getPearsonR());

        switch (direction) {
            case PLUS -> {
                se.setPlusCount(se.getPlusCount() + 1);
                se.setPlusParticipants(se.getPlusParticipants() + participants);
            }
            case MINUS -> {
                se.setMinusCount(se.getMinusCount() + 1);
                se.setMinusParticipants(se.getMinusParticipants() + participants);
            }
            case ZERO -> {
                se.setZeroCount(se.getZeroCount() + 1);
                    se.setZeroParticipants(se.getZeroParticipants() + participants);
            }
        }
        calculateFinalScore(se);
    }

    public static boolean deleteScore(SE se, JSE jse) {
        int participants = (jse.getParticipants() != null && jse.getParticipants() > 0 ) ? jse.getParticipants() : 1;
        BigDecimal score = jse.getScore();

        log.info("📊 [deleteScore] SE: {}, participants: {}, score: {}", se.getId(), participants, score); // 🔥 삭제

        // 총합 점수/참가자 수 반영
        se.setTotalScore(
                se.getTotalScore().subtract(score.multiply(BigDecimal.valueOf(participants)))
        );

        Integer totalParticipants = se.getTotalParticipants() - participants;
        boolean exists = totalParticipants != 0;

        se.setTotalParticipants(totalParticipants);

        Direction direction = countDirection(jse.getCohenD(), jse.getPearsonR());

        switch (direction) {
            case PLUS -> {
                se.setPlusCount(se.getPlusCount() - 1);
                se.setPlusParticipants(se.getPlusParticipants() - participants);
            }
            case MINUS -> {
                se.setMinusCount(se.getMinusCount() - 1);
                se.setMinusParticipants(se.getMinusParticipants() - participants);
            }
            case ZERO -> {
                se.setZeroCount(se.getZeroCount() - 1);
                se.setZeroParticipants(se.getZeroParticipants() - participants);
            }
        }
        calculateFinalScore(se);
        return exists;
    }

    public enum Direction {
        PLUS, MINUS, ZERO
    }

    public static Direction countDirection(BigDecimal cohenD, BigDecimal pearsonR) {
        BigDecimal dScore;
        BigDecimal threshold = new BigDecimal("0.1");

        if (cohenD != null) {
            dScore = cohenD;
        } else if (pearsonR != null) {
            dScore = convertRtoD(pearsonR);
        } else {
            return Direction.ZERO;
        }

        int cmp = dScore.abs().compareTo(threshold);
        if (cmp < 0) {
            return Direction.ZERO;
        } else if (dScore.compareTo(BigDecimal.ZERO) > 0) {
            return Direction.PLUS;
        } else {
            return Direction.MINUS;
        }
    }

    public static BigDecimal calculateDirectionScore(SE se) {
        int total = se.getPlusParticipants() + se.getMinusParticipants() + se.getZeroParticipants();
        if (total == 0) return BigDecimal.ONE;

        int dominant = Math.max(se.getPlusParticipants(),
                Math.max(se.getMinusParticipants(), se.getZeroParticipants()));
        double ratio = (double) dominant / total;

        // 0.5 ~ 1.0 사이로 보정
        return BigDecimal.valueOf(0.5 + 0.5 * ratio).setScale(4, RoundingMode.HALF_UP);
    }

    public static void calculateFinalScore(SE se) {
        if (se.getTotalParticipants() == 0) {
            se.setFinalScore(BigDecimal.ZERO);
        } else {
            BigDecimal baseScore = se.getTotalScore().divide(
                    BigDecimal.valueOf(se.getTotalParticipants()),
                    4,
                    RoundingMode.HALF_UP
            );

            BigDecimal directionScore = calculateDirectionScore(se);
            BigDecimal finalScore = baseScore.multiply(directionScore).setScale(4, RoundingMode.HALF_UP);

            se.setFinalScore(finalScore);
        }

        se.setTier(calculateTier(se.getFinalScore()));
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
