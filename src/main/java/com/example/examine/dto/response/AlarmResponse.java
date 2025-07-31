package com.example.examine.dto.response;

import com.example.examine.entity.Alarm;
import com.example.examine.service.util.TimeService;

import java.time.LocalDate;

public record AlarmResponse (
        Long id,
        String message,
        String link,
        String supplementName,
        String time
) {
        public static AlarmResponse fromEntity(Alarm a) {
            return new AlarmResponse(
                   a.getId(),
                   a.getMessage(),
                   a.getPage() == null ? null : a.getPage().getLink() ,
                   a.getPage() == null ? null : a.getPage().getSupplement().getKorName(),
                   TimeService.relativeTime(a.getCreatedAt())
            );
        }
}
