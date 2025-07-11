package com.example.examine.controller;

import com.example.examine.dto.request.PageRequest;
import com.example.examine.dto.response.PageResponse;
import com.example.examine.service.EntityService.PageService;
import com.example.examine.service.Redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final RedisService redisService;

    @GetMapping("/current")
    public Long getId(@RequestParam String link) {
        Long pageId = pageService.getId(link);
        redisService.incrementPageView(pageId);
        return pageId;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<PageResponse>> sort(@RequestParam(defaultValue = "title") String sort,
                                                     @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        List<PageResponse> pages = pageService.sort(sorting);
        return ResponseEntity.ok(pages);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<PageResponse>> search(@RequestParam String keyword,
                                                     @RequestParam(defaultValue = "title") String sort,
                                                     @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        List<PageResponse> pages = pageService.search(keyword,sorting);
        return ResponseEntity.ok(pages);
    }

    // ➕ 페이지 생성
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> create(@RequestBody PageRequest request) {
        pageService.create(request);
        return ResponseEntity.ok("페이지 생성 완료");
    }

    // ✏️ 페이지 수정
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{pageId}")
    public ResponseEntity<String> update(
            @PathVariable Long pageId,
            @RequestBody PageRequest request
    ) {
        pageService.update(pageId, request);
        return ResponseEntity.ok("페이지 업데이트 완료");
    }

    // ❌ 페이지 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{pageId}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        pageService.delete(id);
        return ResponseEntity.ok("페이지 삭제 완료");
    }
}
