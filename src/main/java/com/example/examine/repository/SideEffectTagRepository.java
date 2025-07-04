package com.example.examine.repository;

import com.example.examine.entity.Tag.Effect.SideEffectTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SideEffectTagRepository extends JpaRepository<SideEffectTag, Long>, TagRepository<SideEffectTag> {
    @Query("""
    select e from SideEffectTag e 
    left join e.se
    """)
    List<SideEffectTag> findAllWithRelation(Sort sort);

    @Query ("""
    select e from SideEffectTag e
    left join e.se
    WHERE LOWER(e.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(e.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<SideEffectTag> searchWithRelation(String keyword, Sort sort);
}
