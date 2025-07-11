package com.example.examine.repository.JSERepository;

import com.example.examine.dto.response.JSEResponse;
import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffectId;
import com.example.examine.entity.SupplementEffect.SE;
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

    @Query("""
    SELECT DISTINCT jse.id.journalId 
    FROM JournalSupplementSideEffect jse
    WHERE jse.id.supplementSideEffectId.sideEffectTagId = :effectId
""")
    List<Long> findJournalsByEffectId(@Param("effectId") Long effectId);

    @Query("""
    SELECT DISTINCT jse.id.journalId 
    FROM JournalSupplementSideEffect jse
    WHERE jse.id.supplementSideEffectId.supplementId = :supplementId
""")
    List<Long> findJournalsBySupplementId(@Param("supplementId") Long supplementId);

    @Query("""
SELECT DISTINCT jse.id.journalId FROM JournalSupplementSideEffect jse
WHERE jse.id.supplementSideEffectId.supplementId IN :supplementIds
""")
    List<Long> findJournalIdsBySupplementIds(@Param("supplementIds") List<Long> supplementIds);

    @Query("""
SELECT DISTINCT jse.id.journalId FROM JournalSupplementSideEffect jse
WHERE jse.id.supplementSideEffectId.sideEffectTagId IN :sideEffectIds
""")
    List<Long> findJournalIdsBySideEffectIds(@Param("sideEffectIds") List<Long> sideEffectIds);

    @Query("""
    SELECT DISTINCT jse
    FROM JournalSupplementSideEffect jse
    left join jse.SE
    WHERE jse.id.journalId = :journalId
""")
    List<JSE> findWithSEByJournalId(@Param("journalId") Long journalId);


    @Query("""
    SELECT DISTINCT jse
    FROM JournalSupplementSideEffect jse
    JOIN jse.SE se
    WHERE jse.journal.id IN :journalIds
""")
    List<JSE> findAllByJournalIdIn(@Param("journalIds") List<Long> journalIds);

}
