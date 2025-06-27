package com.example.examine.dto.response;

import com.example.examine.entity.Page;

import java.time.LocalDateTime;

public record PageResponse(
        Long id,
        String link,
        String title,
        Long viewCount,
        Long bookMarkCount,
        int level,
        LocalDateTime updatedAt
) {
    public static PageResponse fromEntity(Page page){
        return new PageResponse(
                page.getId(),
                page.getLink(),
                page.getTitle(),
                page.getViewCount(),
                page.getBookmarkCount(),
                page.getLevel(),
                page.getUpdatedAt()
        );
    }
}
