package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.request.TagDetailRequest;
import com.example.examine.dto.request.TagRequest;
import com.example.examine.dto.response.*;
import com.example.examine.entity.Page;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.Tag.TypeTag;
import com.example.examine.entity.detail.Detail;
import com.example.examine.entity.detail.EffectDetail;
import com.example.examine.entity.detail.SideEffectDetail;
import com.example.examine.entity.detail.TypeDetail;
import com.example.examine.repository.Detailrepository.DetailRepository;
import com.example.examine.repository.Detailrepository.EffectDetailRepository;
import com.example.examine.repository.Detailrepository.SideEffectDetailRepository;
import com.example.examine.repository.Detailrepository.TypeDetailRepository;
import com.example.examine.repository.JSERepository.JournalSupplementEffectRepository;
import com.example.examine.repository.JSERepository.JournalSupplementSideEffectRepository;
import com.example.examine.repository.PageRepository;
import com.example.examine.repository.SERepository.SupplementEffectRepository;
import com.example.examine.repository.SERepository.SupplementSideEffectRepository;
import com.example.examine.repository.TagRepository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TagService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final TrialDesignRepository trialDesignRepo;
    private final EffectDetailRepository effectDetailRepo;
    private final SideEffectDetailRepository sideEffectDetailRepo;
    private final TypeDetailRepository typeDetailRepo;
    private final BrandRepository brandRepo;
    private final PageRepository pageRepo;
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final JournalSupplementEffectRepository jseRepo;
    private final JournalSupplementSideEffectRepository jsseRepo;
    private final JournalService journalService;

    private final Map<String, TagRepository<? extends Tag>> tagRepoMap = new HashMap<>();
    private final Map<String, DetailRepository<? extends Detail>> detailRepoMap = new HashMap<>();

    @PostConstruct
    private void initTagRepoMap() {
        tagRepoMap.put("type", typeRepo);
        tagRepoMap.put("effect", effectRepo);
        tagRepoMap.put("sideEffect", sideEffectRepo);
        tagRepoMap.put("trialDesign", trialDesignRepo);
        tagRepoMap.put("supplement", supplementRepo);
        tagRepoMap.put("brand", brandRepo);
    }


    @PostConstruct
    private void initDetailRepoMap() {
        detailRepoMap.put("type", typeDetailRepo);
        detailRepoMap.put("effect", effectDetailRepo);
        detailRepoMap.put("sideEffect", sideEffectDetailRepo);
    }

    public ResponseEntity<String> create(TagRequest dto) {
            Tag tag = switch (dto.type()) {
                case "type" -> new TypeTag();
                case "effect" -> new EffectTag();
                case "sideEffect" -> new SideEffectTag();
                default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + dto.type());
            };

            Detail tagDetail = switch (dto.type()) {
                case "type" -> new TypeDetail();
                case "effect" -> new EffectDetail();
                case "sideEffect" -> new SideEffectDetail();
                default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + dto.type());
            };

        @SuppressWarnings("unchecked")
        TagRepository<Tag> repo = (TagRepository<Tag>) tagRepoMap.get(dto.type());
        @SuppressWarnings("unchecked")
        DetailRepository<Detail> detailRepo = (DetailRepository<Detail>) detailRepoMap.get(dto.type());

            tag.setKorName(dto.korName());
            tag.setEngName(dto.engName());
            repo.save(tag);

            tagDetail.setTag(tag);

            detailRepo.save(tagDetail);

        String link = "/detail/tagDetail?id="  + tag.getId().toString();
        Page page = Page.builder()
                .link(link)
                .title(tag.getKorName())
                .level(0)
                .viewCount(0L)
                .bookmarkCount(0L)
                .build();
        pageRepo.save(page);

        return ResponseEntity.ok().build();
    }

    public <T extends Tag> ResponseEntity<String> update(Long id, TagRequest dto) {
        @SuppressWarnings("unchecked")
        TagRepository<T> repo = (TagRepository<T>) tagRepoMap.get(dto.type());

        T tag = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 태그를 찾을 수 없습니다."));
        tag.setKorName(dto.korName());
        tag.setEngName(dto.engName());

        repo.save(tag); // 이제 제네릭 타입 일치
        return ResponseEntity.ok("태그 수정 완료");
    }


    public ResponseEntity<String> updateDetail(TagDetailRequest dto) {
        @SuppressWarnings("unchecked")
        DetailRepository<Detail> repo = (DetailRepository<Detail>) detailRepoMap.get(dto.type());

        if (repo == null) {
            return ResponseEntity.badRequest().body("지원하지 않는 태그 타입입니다: " + dto.type());
        }

        Detail detail = repo.findById(dto.id())
                .orElseThrow(() -> new NoSuchElementException("해당 태그 상세 정보가 없습니다."));

        detail.setIntro(dto.intro());
        detail.setOverview(dto.overview());
        repo.save(detail);

        return ResponseEntity.ok().build();
    }


    public Map<String, List<TagResponse>> sort(List<String> types, Sort sort) {
        Map<String, List<TagResponse>> tagMap = new HashMap<>();

        for (String t : types) {
            TagRepository<?> repo = tagRepoMap.get(t);
            if (repo == null) throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);

            // trialDesign만 특별 정렬 처리
            Sort appliedSort = t.equals("trialDesign")
                    ? Sort.by(Sort.Direction.ASC, "id")
                    : sort;

            List<TagResponse> tags = repo.findAll(appliedSort)
                    .stream()
                    .map(TagResponse::fromEntity)
                    .toList();

            tagMap.put(t, tags);
        }

        return tagMap;
    }

    public Map<String, List<TagResponse>> search(String keyword, List<String> type, Sort sort) {
        Map<String, List<TagResponse>> tagMap = new HashMap<>();

        for (String t : type) {
            TagRepository<?> repo = tagRepoMap.get(t);
            if (repo == null) throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);

            // trialDesign만 특별 정렬 처리
            Sort appliedSort = t.equals("trialDesign")
                    ? Sort.by(Sort.Direction.ASC, "id")
                    : sort;

            List<TagResponse> tags = repo.findByKeyword(keyword, appliedSort)
                    .stream()
                    .map(TagResponse::fromEntity)
                    .toList();

            tagMap.put(t, tags);
        }

        return tagMap;
    }


    public List<?> getTagTable(String type, Sort sort){
        switch (type) {
            case "effect":
                return effectRepo.findAllWithRelation(sort)
                        .stream()
                        .map(EffectTableResponse::fromEntity)
                        .toList();
            case "sideEffect":
                return sideEffectRepo.findAllWithRelation(sort)
                        .stream()
                        .map(EffectTableResponse::fromEntity)
                        .toList();
            case "type":
                return typeRepo.findAllWithRelation(sort)
                        .stream()
                        .map(e->TagTableResponse.fromEntity(e,e.getSt().stream().map(st-> new TagResponse(st.getId().getSupplementId(), st.getSupplementKorName(), st.getSupplementEngName(), "")).toList()))
                        .toList();
            default:
                throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        }
    }

    public List<?> searchEffectTable(String type, String keyword, Sort sort){
        return switch (type) {
            case "effect" -> effectRepo.searchWithRelation(keyword, sort)
                    .stream()
                    .map(EffectTableResponse::fromEntity)
                    .toList();
            case "sideEffect" -> sideEffectRepo.searchWithRelation(keyword, sort)
                    .stream()
                    .map(EffectTableResponse::fromEntity)
                    .toList();
            case "type" -> typeRepo.searchWithRelation(keyword, sort)
                    .stream()
                    .map(e -> TagTableResponse.fromEntity(e, e.getSt().stream().map(st -> new TagResponse(st.getId().getSupplementId(), st.getSupplementKorName(), st.getSupplementEngName(), "")).toList()))
                    .toList();
            default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        };
    }

    public TagDetailResponse getDetail(String type, Long id){
        return switch (type) {
            case "type" -> typeDetailRepo.findById(id)
                    .map(TagDetailResponse::fromEntity)
                    .orElseThrow(() -> new NoSuchElementException("type detail not found"));
            case "effect" -> effectDetailRepo.findById(id)
                    .map(TagDetailResponse::fromEntity)
                    .orElseThrow(() -> new NoSuchElementException("effect detail not found"));
            case "sideEffect" -> sideEffectDetailRepo.findById(id)
                    .map(TagDetailResponse::fromEntity)
                    .orElseThrow(() -> new NoSuchElementException("sideEffect detail not found"));
            default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        };
    }

    public List<SupplementResponse> getSupplements(String type, Long id, Sort sort){
        return switch (type) {
            case "type" -> supplementRepo.findByTypeId(id, sort)
                    .stream()
                    .map(SupplementResponse::fromEntity)
                    .toList();
            case "effect" -> supplementEffectRepo.findSupplementsByEffectId(id, sort)
                    .stream()
                    .map(SupplementResponse::fromEntity)
                    .toList();
            case "sideEffect" -> supplementSideEffectRepo.findSupplementsBySideEffectId(id, sort)
                    .stream()
                    .map(SupplementResponse::fromEntity)
                    .toList();
            default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        };
    }

    public List<JournalResponse> getJournals(String type, Long id){
        return switch (type) {
            case "effect" -> journalService.toJournalResponses(jseRepo.findJournalsByEffectId(id));
            case "sideEffect" -> journalService.toJournalResponses(jsseRepo.findJournalsByEffectId(id));
            default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        };
    }

    public ResponseEntity<String> delete(String type, Long id) {
        @SuppressWarnings("unchecked")
        TagRepository<Tag> repo = (TagRepository<Tag>) tagRepoMap.get(type);

        if (repo == null) {
            return ResponseEntity.badRequest().body("지원하지 않는 태그 타입입니다: " + type);
        }

        try {
            repo.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }



}
