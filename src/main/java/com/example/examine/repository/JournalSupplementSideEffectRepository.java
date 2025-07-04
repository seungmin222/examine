package com.example.examine.repository;

import com.example.examine.dto.response.JSEResponse;
import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface JournalSupplementSideEffectRepository extends JpaRepository<JournalSupplementSideEffect, JournalSupplementSideEffectId> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM JournalSupplementSideEffect e WHERE e.journal.id = :journalId")
    void deleteByJournalId(@Param("journalId") Long journalId);

    Optional<JournalSupplementSideEffect> findById(JournalSupplementSideEffectId id);

    // 혹은 존재 여부만 빠르게 확인할 수도 있어
    boolean existsById(JournalSupplementSideEffectId id);

    List<JournalSupplementSideEffect> findAllByJournalId(Long journalId);
    @Query("""
    SELECT DISTINCT jse.journal 
    FROM JournalSupplementSideEffect jse
    WHERE jse.id.supplementSideEffectId.sideEffectTagId = :effectId
""")
    List<Journal> findJournalsByEffectId(@Param("effectId") Long effectId, Sort sort);

    @Query("""
    SELECT DISTINCT jse.journal 
    FROM JournalSupplementSideEffect jse
    WHERE jse.id.supplementSideEffectId.supplementId = :supplementId
""")
    List<Journal> findJournalsBySupplementId(@Param("supplementId") Long supplementId, Sort sort);

    @Query("""
    SELECT new com.example.examine.dto.response.JSEResponse(
        jse.journal.id,
        se.supplement.id,
        se.sideEffectTag.id,
        se.supplement.korName,
        se.supplement.engName,
        se.sideEffectTag.korName,
        se.sideEffectTag.engName,
        jse.cohenD,
        jse.pearsonR,
        jse.pValue,
        jse.score
    )
    FROM JournalSupplementSideEffect jse
    JOIN jse.SE se
    WHERE jse.journal.id IN :journalIds
""")
    List<JSEResponse> findAllByJournalIdIn(@Param("journalIds") List<Long> journalIds);

    List<JournalSupplementSideEffect> findByJournalId(Long journalId);
}
