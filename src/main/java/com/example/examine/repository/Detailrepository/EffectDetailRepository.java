package com.example.examine.repository.Detailrepository;

import com.example.examine.entity.detail.EffectDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EffectDetailRepository extends JpaRepository<EffectDetail, Long>, DetailRepository<EffectDetail> {
}
