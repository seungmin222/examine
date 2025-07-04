package com.example.examine.repository;

import com.example.examine.entity.detail.TypeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDetailRepository extends JpaRepository<TypeDetail, Long> {
}
