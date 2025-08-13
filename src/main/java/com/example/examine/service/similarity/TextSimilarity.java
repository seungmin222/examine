package com.example.examine.service.similarity;

import com.example.examine.dto.request.TextSimilarityRequest;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.repository.JournalRepository.JournalRepository;
import com.example.examine.repository.TagRepository.TagRepository;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TextSimilarity {

    private static final Logger log = LoggerFactory.getLogger(TextSimilarity.class);
    private static final LevenshteinDistance ld = new LevenshteinDistance();


    public static List<TextSimilarityRequest> findTopKSuggestions(
            String targetText,
            TagRepository<? extends Tag> repository,
            String type,
            int k,
            double threshold
    ) {
        PriorityQueue<AbstractMap.SimpleEntry<TextSimilarityRequest, Double>> pq =
                new PriorityQueue<>(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue));

        final String q = safe(targetText);
        final boolean qHasHangul = containsHangul(q);
        final boolean qIsChoseong = isChoseongOnly(q);
        final String qCho = qIsChoseong ? toChoseong(q) : null;

        final String qEn = normalizeLatin(q); // 영어 contains용

        repository.findAll().forEach(tag -> {
            String kor = safe(tag.getKorName());
            String eng = safe(tag.getEngName());
            String engNorm = normalizeLatin(eng);

            double baseKor = 0.0, baseEng = 0.0;
            if (!kor.isBlank()) {
                baseKor = qIsChoseong ? choseongScore(kor, qCho) : korScore(kor, q);
            }
            if (!eng.isBlank()) {
                baseEng = engScore(eng, q);
            }

            // 쿼리 스크립트 보너스(선택 유지)
            if (qHasHangul) baseKor += 0.03; else baseEng += 0.03;

            // ✅ 부분문자열이면 score에 threshold 그대로 가산
            double scoreKor = baseKor;
            if (!kor.isBlank() && !q.isEmpty() && kor.contains(q)) {
                scoreKor = Math.min(1.0, scoreKor + threshold);
            }

            double scoreEng = baseEng;
            if (!engNorm.isBlank() && !qEn.isEmpty() && engNorm.contains(qEn)) {
                scoreEng = Math.min(1.0, scoreEng + threshold);
            }

            double score = Math.max(scoreKor, scoreEng);
            if (score < threshold) return; // 컷은 최종 점수로

            String label = (scoreKor >= scoreEng) ? kor : eng;
            TextSimilarityRequest req = new TextSimilarityRequest(tag.getId(), label, type, score);
            pqAddTopK(pq, req, score, k);
        });

        return pq.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }

    public static List<TextSimilarityRequest> findTopKJournalSuggestions(
            String targetText,
            JournalRepository repository,
            String type,
            int k,
            double threshold
    ) {
        PriorityQueue<AbstractMap.SimpleEntry<TextSimilarityRequest, Double>> pq =
                new PriorityQueue<>(Comparator.comparingDouble(AbstractMap.SimpleEntry::getValue));

        final String q = safe(targetText);
        if (q.isBlank()) return List.of();
        final String qEn = normalizeLatin(q);

        repository.findAll().forEach(journal -> {
            String title = safe(journal.getTitle());
            if (title.isBlank()) return;

            double base = engScore(title, q); // 0~1
            String titleNorm = normalizeLatin(title);

            // ✅ 부분문자열이면 threshold 그대로 가산
            double score = base;
            if (!qEn.isEmpty() && titleNorm.contains(qEn)) {
                score = Math.min(1.0, base + threshold);
            }

            if (score < threshold) return;

            TextSimilarityRequest req = new TextSimilarityRequest(journal.getId(), title, type, score);
            pqAddTopK(pq, req, score, k);
        });

        return pq.stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(AbstractMap.SimpleEntry::getKey)
                .toList();
    }



// ===== 유틸들 =====

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    private static boolean containsHangul(String s) {
        for (int i = 0; i < s.length(); i++) {
            Character.UnicodeBlock b = Character.UnicodeBlock.of(s.charAt(i));
            if (b == Character.UnicodeBlock.HANGUL_SYLLABLES
                    || b == Character.UnicodeBlock.HANGUL_JAMO
                    || b == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) return true;
        }
        return false;
    }

    // ㄱ-ㅎ만으로 구성된지 (초성 질의) 확인
    private static boolean isChoseongOnly(String s) {
        if (s.isBlank()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '\u3131' || c > '\u314E') return false; // ㄱ(3131)~ㅎ(314E)
        }
        return true;
    }

    // 한글 문자열 -> 초성 문자열
    private static final char[] CHO = {
            'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅃ','ㅅ','ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };
    private static String toChoseong(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0xAC00 && c <= 0xD7A3) { // 완성형 한글
                int idx = c - 0xAC00;
                int choIdx = idx / (21 * 28);
                sb.append(CHO[choIdx]);
            } else if (c >= '\u3131' && c <= '\u314E') {
                sb.append(c); // 이미 초성
            }
            // 그 외 문자는 초성 없음(스킵)
        }
        return sb.toString();
    }

    // 초성 기반 점수: Jaccard(bigram) + prefix 보정
    private static double choseongScore(String kor, String qCho) {
        String nameCho = toChoseong(kor);
        double j = jaccardBigrams(nameCho, qCho);
        if (nameCho.startsWith(qCho)) j = Math.min(1.0, j + 0.2);
        return j;
    }

    // 일반 한글 유사도: Levenshtein 정규화 70% + bigram Jaccard 30%
    private static double korScore(String a, String b) {
        String x = a; String y = b; // 소문자화 불필요, 케이스 없음
        return 0.7 * normalizedLevenshtein(x, y) + 0.3 * jaccardBigrams(x, y);
    }

    // 영문 유사도: 소문자/diacritics 제거 후 Levenshtein 65% + bigram Jaccard 35%
    private static double engScore(String a, String b) {
        String x = normalizeLatin(a);
        String y = normalizeLatin(b);
        return 0.65 * normalizedLevenshtein(x, y) + 0.35 * jaccardBigrams(x, y);
    }

    private static String normalizeLatin(String s) {
        // 간단 케이스 폴딩 (원하면 NFKD + \p{M} 제거까지 확장)
        return s.toLowerCase(Locale.ROOT);
    }

    private static double normalizedLevenshtein(String a, String b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        int max = Math.max(a.length(), b.length());
        if (max == 0) return 1.0;
        int dist = ld.apply(a, b);
        return 1.0 - (double) dist / max;
    }

    private static double jaccardBigrams(String a, String b) {
        Set<String> A = bigrams(a), B = bigrams(b);
        if (A.isEmpty() && B.isEmpty()) return 1.0;
        Set<String> inter = new HashSet<>(A); inter.retainAll(B);
        Set<String> union = new HashSet<>(A); union.addAll(B);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }
    private static Set<String> bigrams(String s) {
        Set<String> set = new HashSet<>();
        for (int i = 0; i < s.length() - 1; i++) set.add(s.substring(i, i + 2));
        return set;
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
