package com.example.examine.repository;

import com.example.examine.entity.Effect.EffectTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EffectTagRepository extends JpaRepository<EffectTag, Long> {
    Optional<EffectTag> findByName(String name);
    List<EffectTag> findByNameContaining(String name, Sort sort);
}
