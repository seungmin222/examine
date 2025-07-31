package com.example.examine.dto.response.UserResponse;

import com.example.examine.dto.response.PageResponse;
import com.example.examine.entity.User.User;
import com.example.examine.entity.User.UserAlarm;
import com.example.examine.entity.User.UserProduct;
import jakarta.persistence.criteria.CriteriaBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        Integer level,
        List<PageResponse> pages,
        List<UserProductResponse> products,
        List<UserAlarmResponse> alarms,
        int unreadCount,
        BigDecimal totalPrice

) {
    public static UserResponse fromEntity(User user) {
        List<UserAlarm> alarms = user.getUserAlarms().stream()
                .sorted(Comparator.comparing((UserAlarm ua) -> ua.getAlarm().getCreatedAt()).reversed())
                .toList();

        List<UserAlarmResponse> alarmResponses = new ArrayList<>();
        int unreadCount = 0;

        for (UserAlarm ua : alarms) {
            if (!ua.isRead()) unreadCount++;
            alarmResponses.add(UserAlarmResponse.fromEntity(ua));
        }

        List<UserProductResponse> products = user.getUserProducts().stream()
                .map(UserProductResponse::fromEntity)
                .toList();

        BigDecimal totalPrice = products.stream()
                .filter(UserProductResponse::isChecked)
                .map(p -> p.product().price().multiply(BigDecimal.valueOf(p.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getLevel(),
                user.getUserPages().stream()
                        .map(e -> PageResponse.fromEntity(e.getPage()))
                        .toList(),
                products,
                alarmResponses,
                unreadCount,
                totalPrice
        );
    }


}
