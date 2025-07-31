package com.example.examine.service.similarity;

import com.example.examine.dto.request.TextSimilarityRequest;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.repository.TagRepository.TagRepository;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TextSimilarity {

    private static final Logger log = LoggerFactory.getLogger(TextSimilarity.class);
    private static final LevenshteinDistance ld = new LevenshteinDistance();

    /**
     * 문자열 정규화 (소문자화 및 알파벳/숫자만 유지)
     */
    public static String normalize(String s) {
        if (s == null) return "";
        return s
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")  // 특수문자 제거, 공백 유지
                .replaceAll("\\s+", " ")         // 공백 정리
                .trim();
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

        if (norm1.contains(norm2) || norm2.contains(norm1)) return 1.0;

        Set<String> tokens1 = new HashSet<>(List.of(norm1.split("\\s+")));
        Set<String> tokens2 = new HashSet<>(List.of(norm2.split("\\s+")));

        if (tokens1.containsAll(tokens2) || tokens2.containsAll(tokens1)) return 0.9;

        // 공통 토큰 유사도 (Jaccard word-level)
        Set<String> intersection = new HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        Set<String> union = new HashSet<>(tokens1);
        union.addAll(tokens2);

        double tokenOverlapScore = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size(); // 0.0 ~ 1.0

        // 기존 유사도 (문자 단위 Levenshtein, Jaccard)
        double levScore = 1.0 - (double) getLevenshtein(norm1, norm2) / Math.max(norm1.length(), norm2.length());
        double jaccardScore = jaccardChar(norm1, norm2);

        // 세 가지 평균 (가중치 조절 가능)
        log.info(String.format("🔍 유사도 계산: [%s] vs [%s] → lev: %.3f, jac: %.3f, token: %.3f",
                s1, s2, levScore, jaccardScore, tokenOverlapScore));

        return levScore * 0.3 + jaccardScore * 0.3 + tokenOverlapScore * 0.4;
    }

    public static List<TextSimilarityRequest> findTopKSuggestions(
            String targetText,
            TagRepository<? extends Tag> repository,
            String type,
            int k,
            double threshold
    ) {

        PriorityQueue<AbstractMap.SimpleEntry<TextSimilarityRequest, Double>> pq = new PriorityQueue<>(
                Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue)
        );

        repository.findAll().forEach(tag -> {
            double score = TextSimilarity.combinedScore(tag.getEngName(), targetText);
            TextSimilarityRequest req = new TextSimilarityRequest(tag.getId(), tag.getEngName(), type);
            pqAddTopK(pq, req, score, k);
        });

        return pq.stream()
                .filter(entry -> entry.getValue() >= threshold)
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 높은 순으로
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }

    public static void pqAddTopK(
            PriorityQueue<AbstractMap.SimpleEntry<TextSimilarityRequest, Double>> pq,
            TextSimilarityRequest item,
            double score,
            int k
    ) {
        if (pq.size() < k) {
            pq.add(new AbstractMap.SimpleEntry<>(item, score));
        } else if (pq.peek().getValue() < score) {
            pq.poll();
            pq.add(new AbstractMap.SimpleEntry<>(item, score));
        }
    }

}
