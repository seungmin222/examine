package com.example.examine.repository;

import com.example.examine.entity.Page;
import com.example.examine.entity.User;
import com.example.examine.entity.UserPage;
import com.example.examine.entity.UserPageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPageRepository extends JpaRepository<UserPage, UserPageId> {
    boolean existsByUserAndPage(User user, Page page);
}

