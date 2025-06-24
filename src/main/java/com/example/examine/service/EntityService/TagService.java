package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.request.TagRequest;
import com.example.examine.dto.request.TierTagRequest;
import com.example.examine.dto.response.TagResponse;
import com.example.examine.dto.response.TierTagResponse;
import com.example.examine.entity.*;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.Effect.SideEffectTag;
import com.example.examine.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TagService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final TrialDesignRepository trialDesignRepo;
    public TagService(SupplementRepository supplementRepo,
                             TypeTagRepository typeRepo,
                             EffectTagRepository effectRepo,
                             SideEffectTagRepository sideEffectRepo,
                             TrialDesignRepository trialDesignRepo) {
        this.supplementRepo = supplementRepo;
        this.typeRepo = typeRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.trialDesignRepo = trialDesignRepo;
    }

    public ResponseEntity<?> create(TagRequest dto) {
        switch (dto.type()) {
            case "type" -> {
                TypeTag tag = new TypeTag();
                tag.setName(dto.name());
                typeRepo.save(tag);
            }
            case "positive" -> {
                EffectTag tag = new EffectTag();
                tag.setName(dto.name());
                effectRepo.save(tag);
            }
            case "negative" -> {
                SideEffectTag tag = new SideEffectTag();
                tag.setName(dto.name());
                sideEffectRepo.save(tag);
            }
            case "trialDesign" -> {
                TrialDesign tag = new TrialDesign();
                tag.setName(dto.name());
                trialDesignRepo.save(tag);
            }
            case "supplement" -> {
                Supplement tag = new Supplement();
                tag.setKorName(dto.name()); // 이름이 korName임에 주의
                supplementRepo.save(tag);
            }
            default -> {
                return ResponseEntity.badRequest().body("지원하지 않는 태그 타입입니다: " + dto.type());
            }
        }

        return ResponseEntity.ok().build();
    }

    // 지금까지 타입을 여러개씩 받고 있었음-> 하나로 변경하니 프론트도 확인 필요
    public List<TagResponse> get(String type, String sort, String direction ){
        List<TagResponse> tag = new ArrayList<>();

            String actualSort = (type.equals("supplement") && sort.equals("name")) ? "korName" : sort;
            Sort sorting = Sort.by(Sort.Direction.fromString(direction), actualSort);
            switch (type) {
                case "type":
                    typeRepo.findAll(sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "positive":
                    effectRepo.findAll(sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "negative":
                    sideEffectRepo.findAll(sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "trialDesign":
                    trialDesignRepo.findAll(Sort.by(Sort.Direction.ASC, "id"))
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "supplement":
                    supplementRepo.findAll(sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getKorName())));
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
            }
        return tag;
    }

    public List<TierTagResponse> get(String type){
        List<TierTagResponse> tag = new ArrayList<>();
        Sort sorting = Sort.by(Sort.Direction.ASC, "id");

            switch (type) {
                case "trialDesign":
                    trialDesignRepo.findAll(sorting)
                            .forEach(e -> tag.add(TierTagResponse.fromEntity(e)));
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 태그 타입: " + type);
            }
        return tag;
    }


    public List<TagResponse> search(String keyword, List<String> type, String sort, String direction ){
        List<TagResponse> tag = new ArrayList<>();

        for (String t : type) {
            String actualSort = (t.equals("supplement") && sort.equals("name")) ? "korName" : sort;
            Sort sorting = Sort.by(Sort.Direction.fromString(direction), actualSort);
            switch (t) {
                case "type":
                    typeRepo.findByNameContaining(keyword,sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "positive":
                    effectRepo.findByNameContaining(keyword,sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "negative":
                    sideEffectRepo.findByNameContaining(keyword,sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getName())));
                    break;
                case "supplement":
                    supplementRepo.findByKorNameContainingIgnoreCaseOrEngNameContainingIgnoreCase(keyword,keyword,sorting)
                            .forEach(e -> tag.add(TagResponse.fromEntity(e.getId(),e.getKorName())));
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);
            }
        }

        return tag;
    }



    public ResponseEntity<?> delete(String type, Long id ){
        boolean[] removed = {false}; // 람다 안에서 값 수정 위해 배열 사용

        switch (type) {
            case "type":
                typeRepo.findById(id).ifPresent(tag -> {
                    typeRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "positive":
                effectRepo.findById(id).ifPresent(tag -> {
                    tag.getSE().clear();// 매핑부터 삭제
                    effectRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "negative":
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
