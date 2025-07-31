package com.example.examine.repository.JournalRepository;

import com.example.examine.entity.Journal;
import com.example.examine.service.util.EnumService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalRepository extends JpaRepository<Journal, Long> {

    @Query("""
SELECT DISTINCT j.id FROM Journal j
LEFT JOIN j.trialDesign t
WHERE (:trialDesign IS NULL OR t.id IN :trialDesign)
  AND (:blind IS NULL OR j.blind = :blind)
  AND (:parallel IS NULL OR j.parallel = :parallel)
""")
    List<Long> findIdsByBasic(
            @Param("trialDesign") List<Long> trialDesign,
            @Param("blind") Integer blind,
            @Param("parallel") Boolean parallel
    );

    @Query("""
    SELECT j.id FROM Journal j
""")
    List<Long> findAllId();

    @Query("""
    SELECT j FROM Journal j
    LEFT JOIN FETCH j.trialDesign
    where j.id IN :ids
""")
    List<Journal> fetchTrialDesignByIds(@Param("ids") List<Long> ids, Sort sort);

    @Query("""
    SELECT j FROM Journal j
    join fetch j.journalSupplementEffects
    where j.id IN :ids
""")
    List<Journal> fetchEffectsByIds(@Param("ids") List<Long> ids);

    @Query("""
    SELECT j FROM Journal j
    join fetch j.journalSupplementSideEffects
    where j.id IN :ids
""")
    List<Journal> fetchSideEffectsByIds(@Param("ids") List<Long> ids);

    boolean existsBySiteTypeAndSiteJournalId(EnumService.JournalSiteType siteType, String siteJournalId);
}
