package com.example.examine.repository;

import com.example.examine.entity.Tag.Effect.EffectTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EffectTagRepository extends JpaRepository<EffectTag, Long>, TagRepository<EffectTag> {
    @Query ("""
    select e from EffectTag e
    left join e.se
    """)
    List<EffectTag> findAllWithRelation(Sort sort);

    @Query ("""
    select e from EffectTag e
    left join e.se
    WHERE LOWER(e.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(e.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<EffectTag> searchWithRelation(String keyword, Sort sort);
}
