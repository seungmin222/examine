package com.example.examine.repository;

import java.util.List;
import java.util.Optional;

import com.example.examine.entity.Tag.TypeTag;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeTagRepository extends JpaRepository<TypeTag, Long> {

    List<TypeTag> findByKorNameContaining(String name, Sort sort);
}
