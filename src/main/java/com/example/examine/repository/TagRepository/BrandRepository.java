package com.example.examine.repository.TagRepository;

import com.example.examine.entity.Tag.Brand;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long>, TagRepository<Brand> {
    @Query("""
    SELECT b FROM Brand b
    WHERE LOWER(b.korName) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(b.engName) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    List<Brand> findByKeyword(@Param("keyword") String keyword, Sort sort);
}
