package com.example.examine.controller;

import java.util.List;

import com.example.examine.dto.request.TagRequest;
import com.example.examine.dto.request.TierTagRequest;

import com.example.examine.dto.response.TagResponse;
import com.example.examine.dto.response.TierTagResponse;
import com.example.examine.service.EntityService.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private static final Logger log = LoggerFactory.getLogger(SupplementController.class);

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<?> addTag(@RequestBody TagRequest dto) {
        log.info("📥 받은 데이터: {}", dto);
        return tagService.create(dto);
    }


    @GetMapping
    public List<TagResponse> getTags(
            @RequestParam String type,
            @RequestParam String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return tagService.get(type, sort, direction);
    }

    @GetMapping("/tier")
    public List<TierTagResponse> getTierTags(@RequestParam String type) {
        return tagService.get(type);
    }


    @GetMapping("/search")
    public List<TagResponse> searchTags(
            @RequestParam String keyword,
            @RequestParam List<String> type,
            @RequestParam String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        return tagService.search(keyword, type, sort, direction);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{type}/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable String type, @PathVariable Long id) {
        return tagService.delete(type, id);
    }

}
