package com.example.examine.entity.User;

import com.example.examine.entity.Alarm;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_alarm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAlarm {

    @EmbeddedId
    private UserAlarmId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("alarmId")
    @JoinColumn(name = "alarm_id")
    private Alarm alarm;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
