package com.example.examine.repository;

import com.example.examine.entity.Pubmed;
import com.example.examine.entity.Supplement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PubmedRepository extends JpaRepository<Pubmed, Long> {
    Optional<Pubmed> findByTitleAndLink(String Title, String Link);
    List<Pubmed> findByTitleContainingIgnoreCase(
            String title, Sort sort
    );

    @Query("""
        SELECT DISTINCT s FROM Pubmed s
        LEFT JOIN s.trial_design t
        LEFT JOIN s.supplements su
        LEFT JOIN s.effects e
        LEFT JOIN s.sideEffects se
        WHERE (:trialDesign IS NULL OR t.id IN :trialDesign)
          AND (:supplementIds IS NULL OR su.id IN :supplementIds)
          AND (:effectIds IS NULL OR e.id IN :effectIds)
          AND (:sideEffectIds IS NULL OR se.id IN :sideEffectIds)
        """)
    List<Pubmed> findFiltered(
            @Param("trialDesign") List<Long> trialDesign,
            @Param("supplementIds") List<Long> supplementIds,
            @Param("effectIds") List<Long> effectIds,
            @Param("sideEffectIds") List<Long> sideEffectIds,
            Sort sort // 명시 안해도 자동정렬
    );
}
