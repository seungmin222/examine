package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.request.TagDetailRequest;
import com.example.examine.dto.request.TagRequest;
import com.example.examine.dto.response.*;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.Tag.TypeTag;
import com.example.examine.entity.detail.EffectDetail;
import com.example.examine.entity.detail.SideEffectDetail;
import com.example.examine.entity.detail.TypeDetail;
import com.example.examine.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final JournalSupplementEffectRepository jseRepo;
    private final JournalSupplementSideEffectRepository jsseRepo;
    private final JournalService journalService;

    public ResponseEntity<String> create(TagRequest dto) {
        switch (dto.type()) {
            case "type" -> {
                TypeTag tag = new TypeTag();
                tag.setKorName(dto.korName());
                tag.setEngName(dto.engName()); // ✅ 추가
                typeRepo.save(tag);

                TypeDetail detail = TypeDetail.builder()
                        .typeTag(tag)
                        .overview("")
                        .intro("")
                        .build();
                typeDetailRepo.save(detail);
            }
            case "effect" -> {
                EffectTag tag = new EffectTag();
                tag.setKorName(dto.korName());
                tag.setEngName(dto.engName()); // ✅ 추가
                effectRepo.save(tag);

                EffectDetail detail = EffectDetail.builder()
                        .effectTag(tag)
                        .overview("")
                        .intro("")
                        .build();
                effectDetailRepo.save(detail);
            }
            case "sideEffect" -> {
                SideEffectTag tag = new SideEffectTag();
                tag.setKorName(dto.korName());
                tag.setEngName(dto.engName()); // ✅ 추가
                sideEffectRepo.save(tag);

                SideEffectDetail detail = SideEffectDetail.builder()
                        .sideEffectTag(tag)
                        .overview("")
                        .intro("")
                        .build();
                sideEffectDetailRepo.save(detail);
            }
            default -> {
                return ResponseEntity.badRequest().body("지원하지 않는 태그 타입입니다: " + dto.type());
            }
        }

        return ResponseEntity.ok().build();
    }


    public ResponseEntity<String> updateDetail(TagDetailRequest dto) {
        switch (dto.type()) {
            case "type" -> {
                TypeDetail detail = typeDetailRepo.findById(dto.id())
                        .orElseThrow(() -> new NoSuchElementException("effect detail not found"));
                detail.setIntro(dto.intro());
                detail.setOverview(dto.overview());
                typeDetailRepo.save(detail);
                return ResponseEntity.ok().build();
            }
            case "effect" -> {
                EffectDetail detail = effectDetailRepo.findById(dto.id())
                        .orElseThrow(() -> new NoSuchElementException("effect detail not found"));
                detail.setIntro(dto.intro());
                detail.setOverview(dto.overview());
                effectDetailRepo.save(detail);
                effectDetailRepo.save(detail);
                return ResponseEntity.ok().build();
            }
            case "sideEffect" -> {
                SideEffectDetail detail = sideEffectDetailRepo.findById(dto.id())
                        .orElseThrow(() -> new NoSuchElementException("effect detail not found"));
                detail.setIntro(dto.intro());
                detail.setOverview(dto.overview());
                sideEffectDetailRepo.save(detail);
                sideEffectDetailRepo.save(detail);
                return ResponseEntity.ok().build();
            }
            default -> {
                return ResponseEntity.badRequest().body("지원하지 않는 태그 타입입니다: " + dto.type());
            }
        }
    }


    public Map<String, List<TagResponse>> get(List<String> type, Sort sort) {
        Map<String, List<TagResponse>> tagMap = new HashMap<>();

        for (String t : type) {
            List<TagResponse> tags = switch (t) {
                case "type" -> typeRepo.findAll(sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "effect" -> effectRepo.findAll(sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "sideEffect" -> sideEffectRepo.findAll(sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "trialDesign" -> trialDesignRepo.findAll(Sort.by(Sort.Direction.ASC, "id"))
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "supplement" -> supplementRepo.findAll(sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);
            };

            tagMap.put(t, tags);
        }

        return tagMap;
    }



    public Map<String, List<TagResponse>> search(String keyword, List<String> type, Sort sort) {
        Map<String, List<TagResponse>> tagMap = new HashMap<>();

        for (String t : type) {
            List<TagResponse> tags = switch (t) {
                case "type" -> typeRepo.findByKorNameContaining(keyword, sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "effect" -> effectRepo.searchWithRelation(keyword, sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "sideEffect" -> sideEffectRepo.searchWithRelation(keyword, sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                case "supplement" -> supplementRepo.searchWithRelations(keyword, sort)
                        .stream()
                        .map(TagResponse::fromEntity)
                        .toList();
                default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);
            };

            tagMap.put(t, tags);
        }

        return tagMap;
    }


    public List<EffectTableResponse> getEffectTable(String type, Sort sort){
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
            default:
                throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        }
    }

    public List<EffectTableResponse> searchEffectTable(String type, String keyword, Sort sort){
        switch (type) {
            case "effect":
                return effectRepo.searchWithRelation(keyword,sort)
                        .stream()
                        .map(EffectTableResponse::fromEntity)
                        .toList();
            case "sideEffect":
                return sideEffectRepo.searchWithRelation(keyword,sort)
                        .stream()
                        .map(EffectTableResponse::fromEntity)
                        .toList();
            default:
                throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        }
    }

    public TagDetailResponse getDetail(String type, Long id){
        switch (type) {
            case "type":
                return typeDetailRepo.findById(id)
                        .map(TagDetailResponse::fromEntity)
                        .orElseThrow(() -> new NoSuchElementException("type detail not found"));
            case "effect":
                return effectDetailRepo.findById(id)
                        .map(TagDetailResponse::fromEntity)
                        .orElseThrow(() -> new NoSuchElementException("effect detail not found"));
            case "sideEffect":
                return sideEffectDetailRepo.findById(id)
                        .map(TagDetailResponse::fromEntity)
                        .orElseThrow(() -> new NoSuchElementException("sideEffect detail not found"));
            default:
                throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        }
    }

    public List<SupplementResponse> getSupplement(String type, Long id, Sort sort){
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

    public List<JournalResponse> getJournal(String type, Long id ,Sort sort){
        return switch (type) {
            case "effect" -> journalService.toJournalResponses(jseRepo.findJournalsByEffectId(id, sort));
            case "sideEffect" -> journalService.toJournalResponses(jsseRepo.findJournalsByEffectId(id, sort));
            default -> throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
        };
    }

    public ResponseEntity<String> delete(String type, Long id ){
        boolean[] removed = {false}; // 람다 안에서 값 수정 위해 배열 사용

        switch (type) {
            case "type":
                typeRepo.findById(id).ifPresent(tag -> {
                    typeRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "effect":
                effectRepo.findById(id).ifPresent(tag -> {
                    tag.getSE().clear();// 매핑부터 삭제
                    effectRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "sideEffect":
                sideEffectRepo.findById(id).ifPresent(tag -> {
                    tag.getSE().clear();
                    sideEffectRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "trialDesign":
                trialDesignRepo.findById(id).ifPresent(tag -> {
                    trialDesignRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "supplement":
                supplementRepo.findById(id).ifPresent(tag -> {
                    supplementRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            default:
                return ResponseEntity.badRequest().body("지원하지 않는 태그 타입입니다: " + type);
        }

        return removed[0] ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

}
