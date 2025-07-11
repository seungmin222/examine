package com.example.examine.repository.TagRepository;

import com.example.examine.dto.response.TagResponse;
import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.Tag.TypeTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementRepository extends JpaRepository<Supplement, Long>, TagRepository<Supplement> { // 애매함

    Optional<Supplement> findByKorNameAndEngName(String korName, String engName);

    @Query("""
    SELECT s.id FROM Supplement s
    WHERE LOWER(s.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(s.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Long> findIdsByKeyword(@Param("keyword") String keyword);

    @Query("""
    SELECT DISTINCT s FROM Supplement s
    LEFT JOIN FETCH s.effects
    WHERE s.id IN :ids
""")
    List<Supplement> fetchEffectsByIds(@Param("ids") List<Long> ids);

    @Query("""
    SELECT DISTINCT s FROM Supplement s
    LEFT JOIN FETCH s.sideEffects
    WHERE s.id IN :ids
""")
    List<Supplement> fetchSideEffectsByIds(@Param("ids") List<Long> ids);

    @Query("""
    SELECT DISTINCT s FROM Supplement s
    LEFT JOIN FETCH s.types
    WHERE s.id IN :ids
""")
    List<Supplement> fetchTypesByIds(@Param("ids") List<Long> ids, Sort sort);

    @Query("SELECT DISTINCT s.id FROM Supplement s")
    List<Long> findAllIds();

    @Query("""
    SELECT s FROM Supplement s
    JOIN s.types t
    WHERE t.id = :typeId
""")
    List<Supplement> findByTypeId(@Param("typeId") Long typeId, Sort sort);

    @Query("SELECT DISTINCT s.id FROM Supplement s LEFT JOIN s.types t WHERE t.id IN :typeIds")
    List<Long> findIdsByTypes(@Param("typeIds") List<Long> typeIds);

    @Query("SELECT DISTINCT s.id FROM Supplement s LEFT JOIN s.effects e WHERE e.effectTag.id IN :effectIds")
    List<Long> findIdsByEffects(@Param("effectIds") List<Long> effectIds);

    @Query("SELECT DISTINCT s.id FROM Supplement s LEFT JOIN s.sideEffects se WHERE se.sideEffectTag.id IN :sideEffectIds")
    List<Long> findIdsBySideEffects(@Param("sideEffectIds") List<Long> sideEffectIds);

    @Query("""
SELECT DISTINCT s.id FROM Supplement s
LEFT JOIN s.effects e
LEFT JOIN s.sideEffects se
WHERE e.tier IN :tiers OR se.tier IN :tiers
""")
    List<Long> findIdsByTiers(@Param("tiers") List<String> tiers);


}
// 함수필드명은 db 칼럼이 아니라 엔티티 필드명임