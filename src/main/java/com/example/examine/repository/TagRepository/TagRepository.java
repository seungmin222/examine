package com.example.examine.repository.TagRepository;

import com.example.examine.dto.response.TagResponse;
import com.example.examine.entity.Tag.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface TagRepository<T extends Tag> extends JpaRepository<T, Long> {
    Optional<T> findByKorNameAndEngName(String kor, String eng);

    default boolean existsByKorNameAndEngName(String korName, String engName) {
        return findByKorNameAndEngName(korName, engName).isPresent();
    }

    @Query("""
        SELECT t FROM #{#entityName} t
        WHERE LOWER(t.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(t.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    List<T> findByKeyword(@Param("keyword") String keyword, Sort sort);
}