package com.example.examine.controller;

import java.util.List;

import com.example.examine.dto.TagRequest;
import com.example.examine.dto.TierTagRequest;

import com.example.examine.service.EntityService.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private static final Logger log = LoggerFactory.getLogger(SupplementController.class);

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    public ResponseEntity<?> addTag(@RequestBody TagRequest dto) {
        log.info("📥 받은 데이터: {}", dto);
        return tagService.create(dto);
    }


    @GetMapping
    public List<TagRequest> getTags(
            @RequestParam List<String> type,
            @RequestParam String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return tagService.get(type, sort, direction);
    }

    @GetMapping("/tier")
    public List<TierTagRequest> getTierTags(@RequestParam List<String> type) {
        return tagService.get(type);
    }


    @GetMapping("/search")
    public List<TagRequest> searchTags(
            @RequestParam String keyword,
            @RequestParam List<String> type,
            @RequestParam String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return tagService.search(keyword, type, sort, direction);
    }


    @DeleteMapping("{type}/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable String type, @PathVariable Long id) {
        return tagService.delete(type, id);
    }

}
