package com.example.examine.repository.Detailrepository;

import com.example.examine.entity.detail.EffectDetail;
import com.example.examine.entity.detail.SideEffectDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SideEffectDetailRepository extends JpaRepository<SideEffectDetail, Long>, DetailRepository<SideEffectDetail> {
}

