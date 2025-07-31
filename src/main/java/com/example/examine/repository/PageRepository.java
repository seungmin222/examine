package com.example.examine.repository;

import com.example.examine.entity.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findByTitleContainingIgnoreCaseOrLinkContainingIgnoreCase(
            String title, String link, Sort sort);

    Optional<Page> findByLink(String link);

    @Query("""
SELECT DISTINCT p 
FROM Page p
join p.supplement
WHERE p.supplement.id IN :supplementIds
""")
    List<Page> findBySupplementIdsWithSupplement(@Param("supplementIds") List<Long> supplementIds);

    @Query("SELECT p FROM Page p WHERE p.supplement.id = :supplementId")
    Optional<Page> findBySupplementId(@Param("supplementId") Long supplementId);

}
