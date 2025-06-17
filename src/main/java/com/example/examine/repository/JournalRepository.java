package com.example.examine.repository;

import com.example.examine.entity.Journal;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<Journal, Long> {
    @Query("""
        SELECT j FROM Journal j
        LEFT JOIN FETCH j.journalSupplementEffects
        WHERE j.id = :id
    """)
    Optional<Journal> findWithEffectsById(@Param("id") Long id);

    @Query("""
        SELECT j FROM Journal j
        LEFT JOIN FETCH j.journalSupplementSideEffects
        WHERE j.id = :id
    """)
    Optional<Journal> findWithSideEffectsById(@Param("id") Long id);
    Optional<Journal> findByLink(String Link);
    List<Journal> findByTitleContainingIgnoreCase(
            String title, Sort sort
    );

    @Query("""
        SELECT DISTINCT s FROM Journal s
        LEFT JOIN s.trialDesign t
        LEFT JOIN s.journalSupplementEffects e
        LEFT JOIN s.journalSupplementSideEffects se
        WHERE (:trialDesign IS NULL OR t.id IN :trialDesign)
         AND (:blind IS NULL OR s.blind IN :blind)
         AND (:parallel IS NULL OR s.parallel IN :parallel)
         AND (
              :supplementIds IS NULL OR
               e.supplement.id IN :supplementIds OR
               se.supplement.id IN :supplementIds
              )
          AND (:effectIds IS NULL OR e.effectTag.id IN :effectIds)
          AND (:sideEffectIds IS NULL OR se.sideEffectTag.id IN :sideEffectIds)
        """)
    List<Journal> findFiltered(
            @Param("trialDesign") List<Long> trialDesign,
            @Param("blind") Integer blind,
            @Param("parallel") Boolean parallel,
            @Param("supplementIds") List<Long> supplementIds,
            @Param("effectIds") List<Long> effectIds,
            @Param("sideEffectIds") List<Long> sideEffectIds,
            Sort sort
    );
}
