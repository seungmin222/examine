package com.example.examine.repository;

import java.util.List;
import java.util.Optional;
import com.example.examine.entity.TrialDesign;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrialDesignRepository extends JpaRepository<TrialDesign, Long> {
    Optional<TrialDesign> findByName(String name);
    List<TrialDesign> findByNameContainingIgnoreCase(String name, Sort sort);
}
