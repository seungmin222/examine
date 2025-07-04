package com.example.examine.repository;

import com.example.examine.dto.response.JSEResponse;
import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface JournalSupplementEffectRepository extends JpaRepository<JournalSupplementEffect, JournalSupplementEffectId> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM JournalSupplementEffect e WHERE e.journal.id = :journalId")
    void deleteByJournalId(@Param("journalId") Long journalId);

    Optional<JournalSupplementEffect> findById(JournalSupplementEffectId id);

    boolean existsById(JournalSupplementEffectId id);

    @Query("""
    SELECT DISTINCT jse.journal 
    FROM JournalSupplementEffect jse
    WHERE jse.id.supplementEffectId.effectTagId = :effectId
""")
    List<Journal> findJournalsByEffectId(@Param("effectId") Long effectId, Sort sort);

    @Query("""
    SELECT DISTINCT jse.journal 
    FROM JournalSupplementEffect jse
    WHERE jse.id.supplementEffectId.supplementId = :supplementId
""")
    List<Journal> findJournalsBySupplementId(@Param("supplementId") Long supplementId, Sort sort);

    @Query("""
    SELECT new com.example.examine.dto.response.JSEResponse(
        jse.journal.id,
        se.supplement.id,
        se.effectTag.id,
        se.supplement.korName,
        se.supplement.engName,
        se.effectTag.korName,
        se.effectTag.engName,
        jse.cohenD,
        jse.pearsonR,
        jse.pValue,
        jse.score
    )
    FROM JournalSupplementEffect jse
    JOIN jse.SE se
    WHERE jse.journal.id IN :journalIds
""")
    List<JSEResponse> findAllByJournalIdIn(@Param("journalIds") List<Long> journalIds);

    List<JournalSupplementEffect> findByJournalId(Long journalId);
}
