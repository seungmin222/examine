package com.example.examine.repository.TagRepository;

import java.util.List;

import com.example.examine.dto.response.TagResponse;
import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.Tag.TypeTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TypeTagRepository extends JpaRepository<TypeTag, Long>, TagRepository<TypeTag> {
    List<TypeTag> findByKorNameContaining(String name, Sort sort);

    @Query ("""
    select t from TypeTag t
    left join t.st
    WHERE LOWER(t.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    OR LOWER(t.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
    """)
    List<TypeTag> searchWithRelation(String keyword, Sort sort);

    @Query ("""
    select t from TypeTag t
    left join t.st
    """)
    List<TypeTag> findAllWithRelation(Sort sort);
}
