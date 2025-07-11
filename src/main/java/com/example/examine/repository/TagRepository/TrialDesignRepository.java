package com.example.examine.repository.TagRepository;

import com.example.examine.dto.response.TagResponse;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.Tag.TrialDesign;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrialDesignRepository extends JpaRepository<TrialDesign, Long>, TagRepository<TrialDesign> {
    Optional<TrialDesign> findByEngName(String name);
}