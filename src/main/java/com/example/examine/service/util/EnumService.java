package com.example.examine.service.util;

import com.example.examine.entity.Tag.Supplement;

public class EnumService {

    public static enum DurationUnit {
        DAY, WEEK, MONTH, YEAR, UNKNOWN;

        public static DurationUnit fromString(String raw) {
            if (raw == null) return null;
            String norm = raw.trim().toLowerCase();

            return switch (norm) {
                case "day", "days" -> DAY;
                case "week", "weeks" -> WEEK;
                case "month", "months" -> MONTH;
                case "year", "years" -> YEAR;
                default -> UNKNOWN;
            };
        }
    }

    public static enum DosageUnit {
        G, MG, UG, IU;

        public static DosageUnit fromString(String raw) {
            if (raw == null) return null;
            String norm = raw.trim().toLowerCase();

            return switch (norm) {
                case "mg" -> MG;
                case "g", "gram", "grams" -> G;
                case "ug", "mcg", "μg", "microgram", "micrograms" -> UG;
                case "iu", "i.u." -> IU;
                default -> null;
            };
        }
    }
}
