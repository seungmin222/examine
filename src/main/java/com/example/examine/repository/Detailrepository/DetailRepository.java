package com.example.examine.repository.Detailrepository;

import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.detail.Detail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DetailRepository<T extends Detail> extends JpaRepository<T, Long> {

}
