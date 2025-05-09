package com.example.examine.repository;

import com.example.examine.entity.SupplementDetail;
import com.example.examine.entity.Supplement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SupplementDetailRepository extends JpaRepository<SupplementDetail, Long> {
    Optional<SupplementDetail> findBySupplement(Supplement supplement);
}
