package com.example.examine.repository;

import com.example.examine.entity.Supplement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementRepository extends JpaRepository<Supplement, Long> { // 애매함
    Optional<Supplement> findByKorNameAndEngName(String korName, String engName);
    List<Supplement> findByKorNameContainingIgnoreCaseOrEngNameContainingIgnoreCase(
            String kor, String eng, Sort sort
    );


    @Query("""
        SELECT DISTINCT s FROM Supplement s
        LEFT JOIN s.types t
        LEFT JOIN s.effectMappings e
        LEFT JOIN s.sideEffectMappings se
        WHERE (:typeIds IS NULL OR t.id IN :typeIds)
          AND (:effectIds IS NULL OR e.effectTag.id IN :effectIds)
          AND (:sideEffectIds IS NULL OR se.sideEffectTag.id IN :sideEffectIds)
        """)
    List<Supplement> findFiltered(
            @Param("typeIds") List<Long> typeIds,
            @Param("effectIds") List<Long> effectIds,
            @Param("sideEffectIds") List<Long> sideEffectIds,
            Sort sort // 명시 안해도 자동정렬
    );
}
// 함수필드명은 db 칼럼이 아니라 엔티티 필드명임