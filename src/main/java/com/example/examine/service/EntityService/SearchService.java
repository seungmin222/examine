package com.example.examine.service.EntityService;

import com.example.examine.dto.request.TextSimilarityRequest;
import com.example.examine.dto.response.SearchSuggestResponse;
import com.example.examine.dto.response.TagResponse;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.repository.Detailrepository.EffectDetailRepository;
import com.example.examine.repository.Detailrepository.SideEffectDetailRepository;
import com.example.examine.repository.Detailrepository.TypeDetailRepository;
import com.example.examine.repository.JournalRepository.JournalRepository;
import com.example.examine.repository.TagRepository.*;
import com.example.examine.service.similarity.TextSimilarity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

// SuggestService.java
@Service
@RequiredArgsConstructor
public class SearchService {
    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final BrandRepository brandRepo;
    private final JournalRepository journalRepo;

    private final Map<String, TagRepository<? extends Tag>> tagRepoMap = new HashMap<>();

    @PostConstruct
    private void initTagRepoMap() {
        tagRepoMap.put("type", typeRepo);
        tagRepoMap.put("effect", effectRepo);
        tagRepoMap.put("sideEffect", sideEffectRepo);
        tagRepoMap.put("supplement", supplementRepo);
        tagRepoMap.put("brand", brandRepo);
    }
    // 너무 짧은 입력/공백은 바로 빈 리스트
    private boolean isTrivial(String s) { return s == null || s.trim().isEmpty(); }

    public List<SearchSuggestResponse> suggestTags(List<String> types, String keyword, int k) {
        if (isTrivial(keyword)) return List.of();

        final double threshold = 0.1; // 노이즈 컷

        // (type:id) 기준으로 중복 제거하면서 순서 유지
        Map<String, SearchSuggestResponse> dedup = new LinkedHashMap<>();

        for (String type : types) {
            @SuppressWarnings("unchecked")
            TagRepository<Tag> repo = (TagRepository<Tag>) tagRepoMap.get(type);
            if (repo == null) throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);

            List<TextSimilarityRequest> list = TextSimilarity.findTopKSuggestions(keyword, repo, type, k, threshold);

            // 리턴이 TextSimilarityRequest(id, name, type) 형태니까 그대로 매핑
            for (var r : list) {
                String key = r.type() + ":" + r.id();
                // score는 유틸에서 안 주니까 일단 null(or 0.0)로
                dedup.putIfAbsent(key, new SearchSuggestResponse(r.id(), r.name(), r.type(), null));
            }
        }

        // 전체에서 최대 k개만
        return dedup.values().stream().limit(k).toList();
    }

    public List<SearchSuggestResponse> suggestJournals(String keyword, int k) {
        if (isTrivial(keyword)) return List.of();

        final double threshold = 0.1; // 노이즈 컷

        // TextSimilarity가 이미 Top-K 정렬/중복 제거된 리스트를 주므로 바로 매핑
        List<TextSimilarityRequest> list =
                TextSimilarity.findTopKJournalSuggestions(keyword, journalRepo, "journal", k, threshold);

        return list.stream()
                .limit(k)
                .map(r -> new SearchSuggestResponse(r.id(), r.name(), r.type(), null)) // score 없음 → null
                .toList();
    }

    public List<SearchSuggestResponse> suggestTotal(String keyword, int k) {
        if (isTrivial(keyword)) return List.of();
        final double threshold = 0.1;

        // (type:id)별 최고점만 유지
        Map<String, SearchSuggestResponse> best = new HashMap<>();
        Map<String, Double> scoreMap = new HashMap<>();

        // 1) 모든 태그 레포
        for (var e : tagRepoMap.entrySet()) {
            String typeKey = e.getKey();
            @SuppressWarnings("unchecked")
            TagRepository<Tag> repo = (TagRepository<Tag>) e.getValue();

            var list = TextSimilarity.findTopKSuggestions(keyword, repo, typeKey, Math.max(k, 20), threshold);
            for (var r : list) {
                String key = r.type() + ":" + r.id();
                double s = r.score() == null ? 0.0 : r.score();
                if (scoreMap.getOrDefault(key, -1.0) < s) {
                    scoreMap.put(key, s);
                    best.put(key, new SearchSuggestResponse(r.id(), r.name(), r.type(), s));
                }
            }
        }

        // 2) 저널
        var jList = TextSimilarity.findTopKJournalSuggestions(keyword, journalRepo, "journal", Math.max(k, 20), threshold);
        for (var r : jList) {
            String key = r.type() + ":" + r.id();
            double s = r.score() == null ? 0.0 : r.score();
            if (scoreMap.getOrDefault(key, -1.0) < s) {
                scoreMap.put(key, s);
                best.put(key, new SearchSuggestResponse(r.id(), r.name(), r.type(), s));
            }
        }

        // 3) 전역 점수 정렬 후 상위 k개
        return best.values().stream()
                .sorted(Comparator.comparingDouble((SearchSuggestResponse x) ->
                        scoreMap.get(x.type() + ":" + x.id())).reversed())
                .limit(k)
                .toList();
    }


}
