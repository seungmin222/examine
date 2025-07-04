package com.example.examine.controller;

import com.example.examine.dto.request.DetailRequest;
import com.example.examine.dto.request.JournalRequest;
import com.example.examine.dto.response.DetailResponse;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.entity.*;
import com.example.examine.service.EntityService.SupplementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;

@RestController
@RequestMapping("/api/details") // api 완전분리해서 자원낭비 최소화
@RequiredArgsConstructor
public class DetailController {
    private final SupplementService supplementService;


    @PutMapping
    @Transactional
    public ResponseEntity<String> update(@RequestBody DetailRequest dto) {
        return supplementService.detailUpdate(dto);
    }

    @GetMapping("/{id}")
    public DetailResponse detail(@PathVariable Long id) {
        return supplementService.detail(id);
    }

    @GetMapping("/{id}/journals")
    public List<JournalResponse> journals(@PathVariable Long id,
                                          @RequestParam(defaultValue = "title") String sort,
                                          @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return supplementService.journals(id, sorting);
    }

}
