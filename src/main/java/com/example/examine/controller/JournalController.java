package com.example.examine.controller;

import com.example.examine.dto.JournalRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import com.example.examine.service.*;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
public class JournalController {

    private final JournalRepository journalRepo;
    private final JournalService journalService;

    public JournalController(JournalRepository journalRepo,
                             JournalService journalService) {
        this.journalRepo = journalRepo;
        this.journalService = journalService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody JournalRequest dto) {
        return journalService.create(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody JournalRequest dto) {
        return journalService.update(id, dto);
    }

    @GetMapping
    public List<JournalRequest> sort(@RequestParam(defaultValue = "title") String sort,
                                @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return journalService.sort(sorting);
    }

    @GetMapping("/search")
    public List<JournalRequest> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return journalService.search(keyword, sorting);
    }

    @GetMapping("/filter")
    public List<JournalRequest> filterByTagIds(
            @RequestParam(required = false) List<Long> trialDesign,
            @RequestParam(required = false) Integer blind,
            @RequestParam(required = false) Boolean parallel,
            @RequestParam(required = false) List<Long> supplementIds,
            @RequestParam(required = false) List<Long> effectIds,
            @RequestParam(required = false) List<Long> sideEffectIds,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return journalService.findFiltered(trialDesign, blind, parallel , supplementIds, effectIds, sideEffectIds, sorting);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return journalService.delete(id);
    }
}