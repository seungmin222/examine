package com.example.examine.controller;

import com.example.examine.dto.DetailRequest;
import com.example.examine.dto.JournalRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import com.example.examine.service.SupplementService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<SupplementDetail> detail(@PathVariable Long id) {
        return supplementService.detail(id);
    }

    @GetMapping("/{id}/journals")
    public List<JournalRequest> journals(@PathVariable Long id) {
        return supplementService.journals(id);
    }

}
