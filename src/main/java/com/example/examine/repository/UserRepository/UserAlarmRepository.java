package com.example.examine.repository.UserRepository;

import com.example.examine.entity.User.UserAlarm;
import com.example.examine.entity.User.UserAlarmId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAlarmRepository extends JpaRepository<UserAlarm, UserAlarmId> {

    void deleteByUserId(Long userId);

    @Modifying
    @Query("UPDATE UserAlarm ua SET ua.isRead = true WHERE ua.user.id = :userId")
    int readAllByUserId(@Param("userId") Long userId);

    @Query("select a from UserAlarm a where a.user.id = :userId order by a.id.alarmId desc")
    List<UserAlarm> findAllByUserId(@Param("userId") Long userId);
}
