package com.example.examine.repository;

import com.example.examine.entity.SideEffectTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SideEffectTagRepository extends JpaRepository<SideEffectTag, Long> {
    Optional<SideEffectTag> findByName(String name);
    List<SideEffectTag> findByNameContaining(String name, Sort sort);
}
