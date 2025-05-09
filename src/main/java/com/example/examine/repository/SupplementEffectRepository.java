package com.example.examine.repository;

import com.example.examine.entity.SupplementEffect;
import com.example.examine.entity.SupplementEffectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

public interface SupplementEffectRepository extends JpaRepository<SupplementEffect, SupplementEffectId> {

    @Modifying
    @Query("DELETE FROM SupplementEffect se WHERE se.supplement.id = :supplementId")
    void deleteBySupplementId(@Param("supplementId") Long supplementId);
}
