package com.example.examine.repository.UserRepository;

import com.example.examine.entity.*;
import com.example.examine.entity.User.User;
import com.example.examine.entity.User.UserPage;
import com.example.examine.entity.User.UserPageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPageRepository extends JpaRepository<UserPage, UserPageId> {
    boolean existsByUserAndPage(User user, Page page);

    @Query("""
    SELECT DISTINCT up
    FROM UserPage up
    JOIN up.page p
    WHERE up.user.id = :userId
""")
    List<Page> findByUserId(@Param("userId") Long userId);

    @Query("SELECT up.id.userId FROM UserPage up WHERE up.id.pageId = :pageId")
    List<Long> findUserIdsByPageId(@Param("pageId") Long pageId);
}

