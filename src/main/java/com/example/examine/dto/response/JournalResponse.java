package com.example.examine.dto.response;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Tag.TrialDesign;

import java.time.LocalDate;
import java.util.List;

public record JournalResponse(
        Long id,
        String title,
        String link,
        List<JSEResponse> effects,
        List<JSEResponse> sideEffects,
        String summary,
        TagResponse trialDesign,
        String blind,
        Boolean parallel,
        DurationResponse duration,
        Integer participants,
        LocalDate date
) {
    public static JournalResponse fromEntity(Journal j) {
        int blind = (j.getBlind() != null ? j.getBlind() : -1);
        String blindStr = switch (blind) {
            case 0 -> "open-label";
            case 1 -> "single-blind";
            case 2 -> "double-blind";
            default -> "unknown";
        };

        return new JournalResponse(
                j.getId(),
                j.getTitle(),
                j.getLink(),
                j.getJournalSupplementEffects()
                        .stream()
                        .map(JSEResponse::fromEntity)
                        .toList(),
                j.getJournalSupplementSideEffects()
                        .stream()
                        .map(JSEResponse::fromEntity)
                        .toList(),
                j.getSummary(),
                TagResponse.fromEntity(j.getTrialDesign()),
                blindStr, j.getParallel(),
                new DurationResponse(j.getDurationValue(), j.getDurationUnit(), j.getDurationDays()),
                j.getParticipants(), j.getDate()
        );
    }
}

