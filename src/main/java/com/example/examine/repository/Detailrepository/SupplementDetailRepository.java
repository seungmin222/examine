package com.example.examine.repository.Detailrepository;

import com.example.examine.entity.detail.SupplementDetail;
import com.example.examine.entity.Tag.Supplement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SupplementDetailRepository extends JpaRepository<SupplementDetail, Long> {
    Optional<SupplementDetail> findBySupplement(Supplement supplement);
}
