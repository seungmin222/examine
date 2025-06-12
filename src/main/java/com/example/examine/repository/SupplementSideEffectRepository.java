package com.example.examine.repository;

import com.example.examine.entity.SupplementSideEffect;
import com.example.examine.entity.SupplementSideEffectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplementSideEffectRepository extends JpaRepository<SupplementSideEffect, SupplementSideEffectId> {

    @Modifying
    @Query("DELETE FROM SupplementSideEffect se WHERE se.supplement.id = :supplementId")
    void deleteBySupplementId(@Param("supplementId") Long supplementId);

    Optional<SupplementSideEffect> findBySupplementIdAndSideEffectTagId(Long supplementId, Long sideEffectTagId);
}
