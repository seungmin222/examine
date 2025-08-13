package com.example.examine.controller;

import com.example.examine.dto.request.JournalRequest;
import com.example.examine.dto.response.JournalResponse;
import com.example.examine.dto.response.TableRespose.DataStructure;
import com.example.examine.dto.response.TableRespose.TableResponse;
import com.example.examine.entity.User.User;
import com.example.examine.service.EntityService.JournalService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

//    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
//    @PostMapping("/batch")
//    public ResponseEntity<String> createBatch(@RequestBody List<JournalRequest> dto) {
//        return journalService.createBatch(dto);
//    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id, @RequestBody JournalRequest dto) {
        return journalService.update(id, dto);
    }

//    @GetMapping("/init")
//    public TableResponse<DataStructure> init(Authentication auth,
//                                               @RequestParam(defaultValue = "title") String sort,
//                                               @RequestParam(defaultValue = "true") Boolean asc,
//                                               @RequestParam(defaultValue = "30") int limit,
//                                               @RequestParam(defaultValue = "0") int offset) {
//        int level = 0;
//        if (auth != null){
//            User user = (User) auth.getPrincipal();
//            if (user != null){
//                level = user.getLevel();
//            }
//        }
//        return journalService.sort(sort,asc,limit,offset,level);
//    }

    @GetMapping
    public TableResponse<DataStructure> get(
            @RequestParam(required = false) List<Long> trialDesign,
            @RequestParam(required = false) List<Integer> blind,
            @RequestParam(required = false) List<Boolean> parallel,
            @RequestParam(required = false) List<Long> supplement,
            @RequestParam(required = false) List<Long> effect,
            @RequestParam(required = false) List<Long> sideEffect,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "true") Boolean asc,
            @RequestParam(defaultValue = "30") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return journalService.get(trialDesign, blind, parallel, supplement, effect, sideEffect, keyword, sort, asc, limit, offset);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        return journalService.delete(id);
    }



}