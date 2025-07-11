package com.example.examine.repository;

import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementEffectId;
import com.example.examine.entity.SupplementType.SupplementType;
import com.example.examine.entity.SupplementType.SupplementTypeId;
import com.example.examine.entity.Tag.Supplement;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplementTypeRepository extends JpaRepository<SupplementType, SupplementTypeId> {

    @Query("""
    SELECT DISTINCT st.supplement
    FROM SupplementType st
    WHERE st.id.typeTagId = :typeId
""")
    List<Supplement> findSupplementsByTypeId(@Param("typeId") Long typeId, Sort sort);

    Optional<SupplementType> findBySupplementIdAndTypeTagId(Long supplementId, Long typeTagId);
}
