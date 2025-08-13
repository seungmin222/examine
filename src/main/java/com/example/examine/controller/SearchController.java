package com.example.examine.controller;

import com.example.examine.dto.response.SearchSuggestResponse;
import com.example.examine.service.EntityService.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/tags")
    public List<SearchSuggestResponse> suggestTags(
            @RequestParam List<String> type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int k
    ) {
        return searchService.suggestTags(type, keyword, k);
    }

    @GetMapping("/journals")
    public List<SearchSuggestResponse> suggestJournals(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int k
    ) {
        return searchService.suggestJournals(keyword, k);
    }

    @GetMapping
    public List<SearchSuggestResponse> suggestTotal(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "10") int k
    ) {
        return searchService.suggestTotal(keyword, k);
    }
}
