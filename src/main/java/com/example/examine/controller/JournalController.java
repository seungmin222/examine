package com.example.examine.controller;

import com.example.examine.dto.request.JournalRequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.repository.*;
import com.example.examine.service.EntityService.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping
    public ResponseEntity<String> create(@RequestBody JournalRequest dto) {
        return journalService.createOne(dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PostMapping("/batch")
    public ResponseEntity<String> createBatch(@RequestBody List<JournalRequest> dto) {
        return journalService.createBatch(dto);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody JournalRequest dto) {
        return journalService.update(id, dto);
    }

    @GetMapping
    public List<JournalResponse> sort(@RequestParam(defaultValue = "title") String sort,
                                @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return journalService.sort(sorting);
    }

    @GetMapping("/search")
    public List<JournalResponse> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return journalService.search(keyword, sorting);
    }

    @GetMapping("/filter")
    public List<JournalResponse> filterByTagIds(
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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return journalService.delete(id);
    }



}