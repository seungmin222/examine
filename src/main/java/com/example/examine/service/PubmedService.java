package com.example.examine.service;

import com.example.examine.entity.Pubmed;
import com.example.examine.repository.PubmedRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

// 서비스
@Service
public class PubmedService {

    private final PubmedRepository pubmedRepo;

    public PubmedService(PubmedRepository pubmedRepo) {
        this.pubmedRepo = pubmedRepo;
    }

    public List<Pubmed> findFiltered(
            List<Long> trialDesign,
            List<Long> supplementIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            Sort sort
    ){
        return pubmedRepo.findFiltered(trialDesign, supplementIds, effectIds, sideEffectIds, sort);
    }
}
