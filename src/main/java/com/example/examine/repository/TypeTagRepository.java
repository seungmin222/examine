package com.example.examine.repository;

import java.util.List;
import java.util.Optional;

import com.example.examine.entity.TrialDesign;
import com.example.examine.entity.TypeTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeTagRepository extends JpaRepository<TypeTag, Long> {
    Optional<TypeTag> findByName(String name);
    List<TypeTag> findByNameContaining(String name, Sort sort);
}
