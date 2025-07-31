package com.example.examine.repository.SERepository;

import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SEId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface SERepository<S extends SE<I>, I extends SEId> extends JpaRepository<S, I> {
    // 공통 메서드 정의 가능
}
