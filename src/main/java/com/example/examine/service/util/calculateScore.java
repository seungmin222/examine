package com.example.examine.service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class calculateScore {

    public static BigDecimal calculateScore(BigDecimal strength, int participants, int duration, String design, int blind) {
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

}
