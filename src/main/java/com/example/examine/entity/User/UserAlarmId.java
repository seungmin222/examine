package com.example.examine.entity.User;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAlarmId implements Serializable {
    private Long userId;
    private Long alarmId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserAlarmId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(alarmId, that.alarmId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, alarmId);
    }
}