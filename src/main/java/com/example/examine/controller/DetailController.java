package com.example.examine.controller;

import com.example.examine.dto.request.DetailRequest;
import com.example.examine.dto.request.JournalRequest;
import com.example.examine.dto.response.DetailResponse;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.entity.*;
import com.example.examine.service.EntityService.SupplementService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;

@RestController
@RequestMapping("/api/details") // api 완전분리해서 자원낭비 최소화
public class DetailController {
    private final SupplementService supplementService;

    public DetailController(SupplementService supplementService) {
        this.supplementService = supplementService;
    }

    @PutMapping ("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DetailRequest dto) {
        return supplementService.detailUpdate(id,dto);
    }

    @GetMapping("/{id}")
    public DetailResponse detail(@PathVariable Long id) {
        return supplementService.detail(id);
    }

    @GetMapping("/{id}/journals")
    public List<JournalResponse> journals(@PathVariable Long id) {
        return supplementService.journals(id);
    }

}
