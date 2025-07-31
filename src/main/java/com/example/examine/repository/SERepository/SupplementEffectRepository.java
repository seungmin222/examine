package com.example.examine.repository.SERepository;

import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementEffectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementEffectRepository extends JpaRepository<SupplementEffect, SupplementEffectId> {

    @Query("""
    SELECT DISTINCT se.supplement
    FROM SupplementEffect se
    WHERE se.id.effectTagId = :effectId
""")
    List<Supplement> findSupplementsByEffectId(@Param("effectId") Long effectId, Sort sort);



}
