package com.example.examine.service;

import com.example.examine.entity.Supplement;
import com.example.examine.repository.SupplementRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

// 서비스
@Service
public class SupplementService {

    private final SupplementRepository supplementRepo;

    public SupplementService(SupplementRepository supplementRepo) {
        this.supplementRepo = supplementRepo;
    }

    public List<Supplement> findFiltered(
            List<Long> typeIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            Sort sort
    ){
        return supplementRepo.findFiltered(typeIds, effectIds, sideEffectIds, sort);
    }
}
