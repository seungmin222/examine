package com.example.examine.repository;

import com.example.examine.entity.Tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TagRepository<T extends Tag> extends JpaRepository<T, Long> {
    // 공통 메서드 정의 가능 (지금은 없어도 됨)
}