package com.example.examine.service.similarity;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TextSimilarity {

    private static final LevenshteinDistance ld = new LevenshteinDistance();

    /**
     * 문자열 정규화 (소문자화 및 알파벳/숫자만 유지)
     */
    public static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    /**
     * 공백 기준 단어 기반 Jaccard 유사도
     */
    public static double jaccardWord(String s1, String s2) {
        Set<String> set1 = new HashSet<>(List.of(s1.split("\\s+")));
        Set<String> set2 = new HashSet<>(List.of(s2.split("\\s+")));

        return computeJaccard(set1, set2);
    }

    /**
     * 문자 단위 기반 Jaccard 유사도
     */
    public static double jaccardChar(String s1, String s2) {
        Set<Character> set1 = s1.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
        Set<Character> set2 = s2.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());

        return computeJaccard(set1, set2);
    }

    /**
     * Jaccard 계산 공통 로직
     */
    private static <T> double computeJaccard(Set<T> set1, Set<T> set2) {
        if (set1.isEmpty() && set2.isEmpty()) return 1.0;
        Set<T> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<T> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    /**
     * Levenshtein 거리 계산
     */
    public static int getLevenshtein(String s1, String s2) {
        if (s1 == null || s2 == null) return Integer.MAX_VALUE;
        return ld.apply(s1, s2);
    }

    /**
     * Levenshtein 거리 기반 유사 여부 판단
     */
    public static boolean isSimilar(String s1, String s2, int threshold) {
        return getLevenshtein(s1, s2) <= threshold;
    }

    /**
     * 정규화된 문자열 기반 조합 유사도 점수 (Levenshtein + Jaccard(Char))
     */

    public static double wordOverlapScore(String s1, String s2) {
        Set<String> set1 = Set.of(normalize(s1).split("\\s+"));
        Set<String> set2 = Set.of(normalize(s2).split("\\s+"));

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        if (set1.isEmpty() || set2.isEmpty()) return 0.0;
        return (double) intersection.size() / Math.min(set1.size(), set2.size());
    }

    public static double combinedScore(String s1, String s2) {
        String norm1 = normalize(s1);
        String norm2 = normalize(s2);

        if (norm1.isEmpty() || norm2.isEmpty()) return 0.0;

        // 완전 포함이면 1.0
        if (norm1.contains(norm2) || norm2.contains(norm1)) return 1.0;

        Set<String> tokens1 = new HashSet<>(List.of(norm1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(List.of(norm2.split("\\s+")));

        if (tokens1.containsAll(tokens2) || tokens2.containsAll(tokens1)) return 0.9;

        double levScore = 1.0 - (double) getLevenshtein(norm1, norm2) / Math.max(norm1.length(), norm2.length());
        double jaccardScore = jaccardChar(norm1, norm2);

        return (levScore + jaccardScore) / 2.0;
    }

}
