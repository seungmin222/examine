package com.example.examine.repository;

import com.example.examine.entity.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    void deleteByCreatedAtBefore(LocalDateTime cutoffTime);
}
