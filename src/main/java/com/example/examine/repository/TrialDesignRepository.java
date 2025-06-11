package com.example.examine.repository;

import com.example.examine.entity.TrialDesign;
import com.example.examine.entity.TypeTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrialDesignRepository extends JpaRepository<TrialDesign, Long> {
    Optional<TrialDesign> findByName(String name);
}