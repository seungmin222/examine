package com.example.examine.dto.response.UserResponse;

import com.example.examine.dto.response.AlarmResponse;
import com.example.examine.entity.User.UserAlarm;

public record UserAlarmResponse(
        AlarmResponse alarm,
        boolean isRead
) {
    public static UserAlarmResponse fromEntity(UserAlarm u){
        return new UserAlarmResponse(
                AlarmResponse.fromEntity(u.getAlarm()),
                u.isRead()
        );
    }
}
