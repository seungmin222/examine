package com.example.examine.repository;

import com.example.examine.entity.detail.SideEffectDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SideEffectDetailRepository extends JpaRepository<SideEffectDetail, Long> {
}

