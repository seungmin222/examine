package com.example.examine.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import com.example.examine.dto.TagRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.TypeTagRepository;
import com.example.examine.repository.EffectTagRepository;
import com.example.examine.repository.SideEffectTagRepository;
import com.example.examine.repository.SupplementRepository;
import com.example.examine.repository.TrialDesignRepository;

import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TypeTagRepository typeRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final SupplementRepository supplementRepo;
    private final TrialDesignRepository trialDesignRepo;

    public TagController(TypeTagRepository typeRepo,
                         EffectTagRepository effectRepo,
                         SideEffectTagRepository sideEffectRepo,
                         SupplementRepository supplementRepo,
                         TrialDesignRepository trialDesignRepo) {
        this.typeRepo = typeRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.supplementRepo = supplementRepo;
        this.trialDesignRepo = trialDesignRepo;
    }

    @PostMapping
    public ResponseEntity<?> addTag(@RequestBody TagRequest dto) {
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


    @GetMapping
    public List<TagRequest> getTags(
            @RequestParam List<String> type,
            @RequestParam String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        List<TagRequest> tag = new ArrayList<>();

        for (String t : type) {
            String actualSort = (t.equals("supplement") && sort.equals("name")) ? "korName" : sort;
            Sort sorting = Sort.by(Sort.Direction.fromString(direction), actualSort);
            switch (t) {
                case "type":
                    typeRepo.findAll(sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "type")));
                    break;
                case "positive":
                    effectRepo.findAll(sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "positive")));
                    break;
                case "negative":
                    sideEffectRepo.findAll(sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "negative")));
                    break;
                case "trialDesign":
                    trialDesignRepo.findAll(sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "trialDesign")));
                    break;
                case "supplement":
                    supplementRepo.findAll(sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getKorName(), "supplement")));
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);
            }
        }

        return tag;
    }


    @GetMapping("/search")
    public List<TagRequest> searchTags(
            @RequestParam String keyword,
            @RequestParam List<String> type,
            @RequestParam String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        List<TagRequest> tag = new ArrayList<>();

        for (String t : type) {
            String actualSort = (t.equals("supplement") && sort.equals("name")) ? "korName" : sort;
            Sort sorting = Sort.by(Sort.Direction.fromString(direction), actualSort);
            switch (t) {
                case "type":
                    typeRepo.findByNameContaining(keyword,sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "type")));
                    break;
                case "positive":
                    effectRepo.findByNameContaining(keyword,sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "positive")));
                    break;
                case "negative":
                    sideEffectRepo.findByNameContaining(keyword,sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "negative")));
                    break;
                case "trialDesign":
                    trialDesignRepo.findByNameContainingIgnoreCase(keyword,sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getName(), "trialDesign")));
                    break;
                case "supplement":
                    supplementRepo.findByKorNameContainingIgnoreCaseOrEngNameContainingIgnoreCase(keyword,keyword,sorting)
                            .forEach(e -> tag.add(new TagRequest(e.getId().longValue(), e.getKorName(), "supplement")));
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 태그 타입: " + t);
            }
        }

        return tag;
    }

    @DeleteMapping("{type}/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable String type, @PathVariable Long id) {
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
                    effectRepo.delete(tag);
                    removed[0] = true;
                });
                break;
            case "negative":
                sideEffectRepo.findById(id).ifPresent(tag -> {
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
