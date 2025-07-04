package com.example.examine.repository;

import com.example.examine.entity.Tag.TrialDesign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrialDesignRepository extends JpaRepository<TrialDesign, Long> {
    Optional<TrialDesign> findByEngName(String name);
}