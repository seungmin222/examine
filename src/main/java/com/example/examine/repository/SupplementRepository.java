package com.example.examine.repository;

import com.example.examine.entity.Tag.Supplement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementRepository extends JpaRepository<Supplement, Long>, TagRepository<Supplement> { // 애매함
    Optional<Supplement> findByKorNameAndEngName(String korName, String engName);
    @Query("""
    SELECT DISTINCT s FROM Supplement s
    LEFT JOIN FETCH s.effects
    LEFT JOIN FETCH s.sideEffects
    LEFT JOIN FETCH s.types
    WHERE LOWER(s.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(s.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Supplement> searchWithRelations(@Param("keyword") String keyword, Sort sort);


    @Query("""
SELECT DISTINCT s FROM Supplement s
LEFT JOIN FETCH s.effects
LEFT JOIN FETCH s.sideEffects
LEFT JOIN FETCH s.types
""")
    List<Supplement> findAllWithRelations(Sort sort);

    @Query("""
    SELECT s FROM Supplement s
    JOIN s.types t
    WHERE t.id = :typeId
""")
    List<Supplement> findByTypeId(@Param("typeId") Long typeId, Sort sort);

    @Query("""
        SELECT DISTINCT s FROM Supplement s
        LEFT JOIN s.types t
        LEFT JOIN s.effects e
        LEFT JOIN s.sideEffects se
        WHERE (:types IS NULL OR t.id IN :types)
          AND (:effectIds IS NULL OR e.effectTag.id IN :effectIds)
          AND (:sideEffectIds IS NULL OR se.sideEffectTag.id IN :sideEffectIds)
          AND (:tiers IS NULL OR 
                e.tier IN :tiers OR
                se.tier IN :tiers)
        """)
    List<Supplement> findFiltered(
            @Param("types") List<Long> typeIds,
            @Param("effectIds") List<Long> effectIds,
            @Param("sideEffectIds") List<Long> sideEffectIds,
            @Param("tiers") List<String> tiers,
            Sort sort // 명시 안해도 자동정렬
    );
}
// 함수필드명은 db 칼럼이 아니라 엔티티 필드명임