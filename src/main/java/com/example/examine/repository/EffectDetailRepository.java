package com.example.examine.repository;

import com.example.examine.entity.detail.EffectDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EffectDetailRepository extends JpaRepository<EffectDetail, Long> {
}
