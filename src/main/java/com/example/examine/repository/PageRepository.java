package com.example.examine.repository;

import com.example.examine.entity.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {
    Optional<Page> findByTitleContainingIgnoreCaseOrLinkContainingIgnoreCase(
            String title, String link, Sort sort);

    Optional<Page> findByLink(String link);
}
