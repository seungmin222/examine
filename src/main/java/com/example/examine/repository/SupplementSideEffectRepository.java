package com.example.examine.repository;

import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementSideEffectRepository extends JpaRepository<SupplementSideEffect, SupplementSideEffectId> {

    @Modifying
    @Query("DELETE FROM SupplementSideEffect se WHERE se.supplement.id = :supplementId")
    void deleteBySupplementId(@Param("supplementId") Long supplementId);

    @Query("""
        SELECT se.supplement
        FROM SupplementSideEffect se
        WHERE se.id.sideEffectTagId = :sideEffectId
    """)
    List<Supplement> findSupplementsBySideEffectId(@Param("sideEffectId") Long sideEffectId, Sort sort);

    Optional<SupplementSideEffect> findBySupplementIdAndSideEffectTagId(Long supplementId, Long sideEffectTagId);
}
