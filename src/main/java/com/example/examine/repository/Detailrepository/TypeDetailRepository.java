package com.example.examine.repository.Detailrepository;

import com.example.examine.entity.detail.EffectDetail;
import com.example.examine.entity.detail.TypeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypeDetailRepository extends JpaRepository<TypeDetail, Long>, DetailRepository<TypeDetail> {
}
