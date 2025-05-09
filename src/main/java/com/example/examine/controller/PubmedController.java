package com.example.examine.controller;

import com.example.examine.dto.PubmedRequest;
import com.example.examine.entity.*;
import com.example.examine.repository.*;
import com.example.examine.service.*;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/pubmeds")
public class PubmedController {

    private final PubmedRepository pubmedRepo;
    private final TrialDesignRepository trialDesignRepo;
    private final SupplementRepository supplementRepo;
    private final EffectTagRepository effectRepo;
    private final SideEffectTagRepository sideEffectRepo;
    private final PubmedService pubmedService;

    public PubmedController(PubmedRepository pubmedRepo,
                                    TrialDesignRepository trialDesignRepo,
                                    SupplementRepository supplementRepo,
                                    EffectTagRepository effectRepo,
                                    SideEffectTagRepository sideEffectRepo,
                                    PubmedService pubmedService) {
        this.pubmedRepo = pubmedRepo;
        this.trialDesignRepo = trialDesignRepo;
        this.supplementRepo = supplementRepo;
        this.effectRepo = effectRepo;
        this.sideEffectRepo = sideEffectRepo;
        this.pubmedService = pubmedService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PubmedRequest dto) {
        if (pubmedRepo.findByTitleAndLink(dto.title(), dto.link()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 논문이 존재합니다.");
        }

        Pubmed s = new Pubmed();
        s.setTitle(dto.title());
        s.setLink(dto.link());
        s.setSummary(dto.summary());
        s.setTrial_length(dto.trial_length());
        s.setParticipants(dto.participants());

        if (dto.trial_design_id() != null) {
            trialDesignRepo.findById(dto.trial_design_id()).ifPresent(s::setTrial_design);
        }

        if (dto.supplements() != null) {
            s.setSupplements(supplementRepo.findAllById(dto.supplements()));
        }

        if (dto.effects() != null) {
            s.setEffects(effectRepo.findAllById(dto.effects()));
        }

        if (dto.sideEffects() != null) {
            s.setSideEffects(sideEffectRepo.findAllById(dto.sideEffects()));
        }

        Pubmed saved = pubmedRepo.save(s);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PubmedRequest dto) {
        Optional<Pubmed> opt = pubmedRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Pubmed s = opt.get();
        s.setTitle(dto.title());
        s.setLink(dto.link());
        s.setSummary(dto.summary());
        s.setTrial_length(dto.trial_length());
        s.setParticipants(dto.participants());
        if (dto.trial_design_id() != null) {
            trialDesignRepo.findById(dto.trial_design_id()).ifPresent(s::setTrial_design);
        } else {
            s.setTrial_design(null);
        }
        s.setSupplements(dto.supplements() != null ? supplementRepo.findAllById(dto.supplements()) : List.of());
            s.setEffects(dto.effects() != null ? effectRepo.findAllById(dto.effects()) : List.of());
            s.setSideEffects(dto.sideEffects() != null ? sideEffectRepo.findAllById(dto.sideEffects()) : List.of());

        Pubmed updated = pubmedRepo.save(s);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public List<Pubmed> sortBy(@RequestParam(defaultValue = "title") String sort,
                                   @RequestParam(defaultValue = "asc") String direction) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return pubmedRepo.findAll(sorting);
    }

    @GetMapping("/search")
    public List<Pubmed> searchAndSort(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return pubmedRepo.findByTitleContainingIgnoreCase(
                keyword, sorting
        );
    }

    @GetMapping("/filter")
    public List<Pubmed> filterByTagIds(
            @RequestParam(required = false) List<Long> trialDesign,
            @RequestParam(required = false) List<Long> supplementIds,
            @RequestParam(required = false) List<Long> effectIds,
            @RequestParam(required = false) List<Long> sideEffectIds,
            @RequestParam(defaultValue = "title") String sort,       // ✅ 기본값 추가하면 더 안전
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sorting = Sort.by(Sort.Direction.fromString(direction), sort);
        return pubmedService.findFiltered(trialDesign, supplementIds, effectIds, sideEffectIds, sorting);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!pubmedRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        pubmedRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}

