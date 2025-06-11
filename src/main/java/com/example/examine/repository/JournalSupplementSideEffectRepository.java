package com.example.examine.repository;

import com.example.examine.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JournalSupplementSideEffectRepository extends JpaRepository<JournalSupplementSideEffect, JournalSupplementSideEffectId> {
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM JournalSupplementSideEffect e WHERE e.journal.id = :journalId")
    void deleteByJournalId(@Param("journalId") Long journalId);

    Optional<JournalSupplementSideEffect> findById(JournalSupplementSideEffectId id);

    // 혹은 존재 여부만 빠르게 확인할 수도 있어
    boolean existsById(JournalSupplementSideEffectId id);
}
