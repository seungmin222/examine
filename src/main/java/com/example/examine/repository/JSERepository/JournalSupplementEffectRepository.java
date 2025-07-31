package com.example.examine.repository.JSERepository;

import com.example.examine.dto.response.JSEResponse;
import com.example.examine.entity.Journal;
import com.example.examine.entity.JournalSupplementEffect.JSE;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffectId;
import com.example.examine.entity.SupplementEffect.SE;
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
    left join jse.journal.trialDesign
    WHERE jse.id.supplementEffectId.effectTagId = :effectId
""")
    List<Journal> findJournalsByEffectId(@Param("effectId") Long effectId);

    @Query("""
    SELECT DISTINCT jse.journal
    FROM JournalSupplementEffect jse
    left join jse.journal.trialDesign
    WHERE jse.id.supplementEffectId.supplementId = :supplementId
""")
    List<Journal> findJournalsBySupplementId(@Param("supplementId") Long supplementId);

    @Query("""
SELECT DISTINCT jse.journal
FROM JournalSupplementEffect jse
left join jse.journal.trialDesign
WHERE jse.id.supplementEffectId.supplementId IN :supplementIds
""")
    List<Journal> findJournalsBySupplementIds(@Param("supplementIds") List<Long> supplementIds);

    @Query("""
SELECT DISTINCT jse.journal
FROM JournalSupplementEffect jse
left join jse.journal.trialDesign
WHERE jse.id.supplementEffectId.effectTagId IN :effectIds
""")
    List<Journal> findJournalsByEffectIds(@Param("effectIds") List<Long> effectIds);


    @Query("""
    SELECT DISTINCT jse
    FROM JournalSupplementEffect jse
    left join jse.SE
    WHERE jse.id.journalId = :journalId
""")
    List<JSE> findWithSEByJournalId(@Param("journalId") Long journalId);

    @Query("""
    SELECT DISTINCT jse
    FROM JournalSupplementEffect jse
    JOIN jse.SE se
    WHERE jse.journal.id IN :journalIds
""")
    List<JSE> findAllByJournalIdIn(@Param("journalIds") List<Long> journalIds);

    @Query("""
    SELECT jse FROM JournalSupplementEffect jse
    JOIN FETCH jse.SE se
    WHERE jse.journal.id IN :journalIds
""")
    List<JournalSupplementEffect> fetchJSEWithSEByJournalIds(@Param("journalIds") List<Long> journalIds);

}
