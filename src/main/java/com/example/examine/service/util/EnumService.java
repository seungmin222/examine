package com.example.examine.service.util;

public class EnumService {

    public static enum DurationUnit {
        DAY, WEEK, MONTH, YEAR, NULL;

        public static DurationUnit fromString(String raw) {
            if (raw == null) return null;
            String norm = raw.trim().toLowerCase();

            return switch (norm) {
                case "day", "days" -> DAY;
                case "week", "weeks" -> WEEK;
                case "month", "months" -> MONTH;
                case "year", "years" -> YEAR;
                default -> NULL;
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case DAY -> "Day";
                case WEEK -> "Week";
                case MONTH -> "Month";
                case YEAR -> "Year";
                case NULL -> "null";
            };
        }
    }

    public static enum DosageUnit {
        G, MG, UG, IU, CFU, NULL;

        public static DosageUnit fromString(String raw) {
            if (raw == null) return null;
            String norm = raw.trim().toLowerCase();

            return switch (norm) {
                case "mg" -> MG;
                case "g", "gram", "grams" -> G;
                case "ug", "mcg", "μg", "microgram", "micrograms" -> UG;
                case "iu", "i.u." -> IU;
                case "cfu" -> CFU;
                default -> NULL;
            };
        }

        @Override
        public String toString() {
            return switch (this) {
                case MG -> "mg";
                case G -> "g";
                case UG -> "ug";
                case IU -> "IU";
                case CFU -> "CFU";
                case NULL -> "null";
            };
        }
    }

    public enum ProductSiteType {

        IHERB("iHerb", "https://kr.iherb.com/pr/"),
        COUPANG("Coupang", "https://www.coupang.com/vp/products/"),
        NAVER("NaverStore", "https://smartstore.naver.com/"),
        NULL("Unknown", "");

        private final String label;
        private final String baseUrl;

        ProductSiteType(String label, String baseUrl) {
            this.label = label;
            this.baseUrl = baseUrl;
        }

        public String buildUrl(String productId) {
            return baseUrl + productId;
        }

        public static ProductSiteType fromString(String type) {
            for (ProductSiteType t : values()) {
                if (t.name().equalsIgnoreCase(type) || t.label.equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return NULL;
        }

        @Override
        public String toString() {
            return label;
        }

        public static ProductSiteType fromLink(String link) {
            if (link == null) return NULL;
            String lower = link.toLowerCase();

            if (lower.contains("iherb.com")) return IHERB;
            if (lower.contains("coupang.com")) return COUPANG;
            if (lower.contains("smartstore.naver.com")) return NAVER;

            return NULL;
        }
    }

    public enum JournalSiteType {

        PUBMED("PubMed", "https://pubmed.ncbi.nlm.nih.gov/"),
        CLINICAL_TRIALS("ClinicalTrials", "https://clinicaltrials.gov/ct2/show/"),
        SEMANTIC_SCHOLAR("SemanticScholar", "https://www.sciencedirect.com/science/article/pii/"),
        NULL("null", "");

        private final String label;
        private final String baseUrl;

        JournalSiteType(String label, String baseUrl) {
            this.label = label;
            this.baseUrl = baseUrl;
        }

        public String getLabel() {
            return label;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String buildUrl(String journalId) {
            return baseUrl + journalId;
        }

        public static JournalSiteType fromString(String type) {
            for (JournalSiteType t : values()) {
                if (t.name().equalsIgnoreCase(type) || t.label.equalsIgnoreCase(type)) {
                    return t;
                }
            }
            return NULL;
        }

        @Override
        public String toString() {
            return label;
        }

        public static JournalSiteType fromLink(String link) {
            if (link == null) return NULL;
            String lower = link.toLowerCase();
            if (lower.contains("pubmed.ncbi.nlm.nih.gov")) return PUBMED;
            if (lower.contains("clinicaltrials.gov")) return CLINICAL_TRIALS;
            if (lower.contains("semanticscholar")) return SEMANTIC_SCHOLAR;

            return NULL;
        }
    }

}
