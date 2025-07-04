package com.example.examine.controller;

import java.util.List;
import java.util.Map;

import com.example.examine.dto.request.TagDetailRequest;
import com.example.examine.dto.request.TagRequest;

import com.example.examine.dto.response.*;
import com.example.examine.service.EntityService.TagService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    private final TagService tagService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<String> addTag(@RequestBody TagRequest dto) {
        log.info("📥 받은 데이터: {}", dto);
        return tagService.create(dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/detail")
    @Transactional
    public ResponseEntity<String> update(@RequestBody TagDetailRequest dto) {
        return tagService.updateDetail(dto);
    }

    @GetMapping
    public Map<String, List<TagResponse>> getTags(
            @RequestParam List<String> type,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return tagService.get(type, sorting);
    }

    @GetMapping("/search")
    public Map<String, List<TagResponse>> searchTags(
            @RequestParam String keyword,
            @RequestParam List<String> type,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return tagService.search(keyword, type, sorting);
    }

    @GetMapping("/detail/supplements/{type}/{id}")
    public List<SupplementResponse>getSupplement(@PathVariable String type,
                                    @PathVariable Long id,
                                    @RequestParam(defaultValue = "id") String sort,
                                    @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return tagService.getSupplement(type, id, sorting);
    }

    @GetMapping("/detail/journals/{type}/{id}")
    public List<JournalResponse> getJournal(@PathVariable String type,
                                    @PathVariable Long id,
                                    @RequestParam(defaultValue = "id") String sort,
                                    @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return tagService.getJournal(type, id, sorting);
    }


    @GetMapping("/detail/{type}/{id}")
    public TagDetailResponse detail(@PathVariable String type,
                                    @PathVariable Long id) {
        return tagService.getDetail(type, id);
    }

    @GetMapping("/table")
    public List<EffectTableResponse> table(@RequestParam(defaultValue = "positive") String type,
                                   @RequestParam(defaultValue = "id") String sort,
                                   @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return tagService.getEffectTable(type, sorting);
    }

    @GetMapping("/table/{keyword}")
    public List<EffectTableResponse> tableSearch(@PathVariable String keyword,
                                                 @RequestParam String type,
                                           @RequestParam(defaultValue = "id") String sort,
                                           @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return tagService.searchEffectTable(type, keyword, sorting);
    }



    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{type}/{id}")
    public ResponseEntity<String> deleteTag(@PathVariable String type, @PathVariable Long id) {
        return tagService.delete(type, id);
    }

}
