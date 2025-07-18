package com.example.examine.repository;

import com.example.examine.entity.Product;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
    SELECT p FROM Product p
    left join p.supplementDetail s
    where s.supplementId = :supplementId
""")
    List<Product> findBySupplementId(Long supplementId, Sort sort);

    List<Product> findByBrandId(Long brandId);

    List<Product> findByNameContainingIgnoreCase(String name);
}
