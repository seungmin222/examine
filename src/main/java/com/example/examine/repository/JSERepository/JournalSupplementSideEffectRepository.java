package com.example.examine.repository.JSERepository;

import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalSupplementSideEffectRepository extends JpaRepository<JournalSupplementSideEffect, JournalSupplementSideEffectId> {

    Optional<JournalSupplementSideEffect> findById(JournalSupplementSideEffectId id);

    boolean existsById(JournalSupplementSideEffectId id);

    @Query("""
    SELECT DISTINCT jse.journal 
    FROM JournalSupplementSideEffect jse
    left join jse.journal.trialDesign
    WHERE jse.id.supplementSideEffectId.sideEffectTagId = :effectId
""")
    List<Journal> findJournalsByEffectId(@Param("effectId") Long effectId);

    @Query("""
    SELECT DISTINCT jse.journal
    FROM JournalSupplementSideEffect jse
    left join jse.journal.trialDesign
    WHERE jse.id.supplementSideEffectId.supplementId = :supplementId
""")
    List<Journal> findJournalsBySupplementId(@Param("supplementId") Long supplementId);

    @Query("""
SELECT DISTINCT jse.journal 
FROM JournalSupplementSideEffect jse
left join jse.journal.trialDesign
WHERE jse.id.supplementSideEffectId.supplementId IN :supplementIds
""")
    List<Journal> findJournalsBySupplementIds(@Param("supplementIds") List<Long> supplementIds);

    @Query("""
SELECT DISTINCT jse.journal 
FROM JournalSupplementSideEffect jse
left join jse.journal.trialDesign
WHERE jse.id.supplementSideEffectId.sideEffectTagId IN :sideEffectIds
""")
    List<Journal> findJournalsBySideEffectIds(@Param("sideEffectIds") List<Long> sideEffectIds);

    @Query("""
    SELECT DISTINCT jse
    FROM JournalSupplementSideEffect jse
    left join jse.SE
    WHERE jse.id.journalId = :journalId
""")
    List<JSE> findWithSEByJournalId(@Param("journalId") Long journalId);


    @Query("""
    SELECT jsse FROM JournalSupplementSideEffect jsse
    JOIN FETCH jsse.SE sse
    WHERE jsse.journal.id IN :journalIds
""")
    List<JournalSupplementSideEffect> fetchJSSEWithSideEffectByJournalIds(@Param("journalIds") List<Long> journalIds);

}
