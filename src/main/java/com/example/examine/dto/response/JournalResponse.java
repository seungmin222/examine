package com.example.examine.dto.response;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.service.util.EnumService;

import java.time.LocalDate;
import java.util.List;

public record JournalResponse(
        Long id,
        String title,
        String link,
        String siteType,
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
    public static JournalResponse fromEntity(Journal j, List<JSEResponse> effects, List<JSEResponse> sideEffects) {
        EnumService.JournalSiteType siteType = j.getSiteType();

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
                siteType.buildUrl(j.getSiteJournalId()),
                siteType.toString(),
                effects,
                sideEffects,
                j.getSummary(),
                TagResponse.fromEntity(j.getTrialDesign()),
                blindStr, j.getParallel(),
                new DurationResponse(j.getDurationValue(), j.getDurationUnit().toString(), j.getDurationDays()),
                j.getParticipants(), j.getDate()
        );
    }
}

