package com.example.examine.dto;

import com.example.examine.entity.*;
import java.util.List;
import java.time.LocalDate;

public record JournalRequest(
        Long id,
        String title,
        String link,
        List<JournalEffectRequest> effects,
        List<JournalSideEffectRequest> sideEffects,
        String summary,
        TierTagRequest trialDesign,
        String blind,
        Boolean parallel,
        DurationRequest duration,
        Integer participants,
        LocalDate date
) {
    private static final String[] blindLabel = {
            "open-label", "single-blind", "double-blind"
    };

    public static JournalRequest fromEntity(Journal journal) {
        TrialDesign td = journal.getTrial_design();
        TierTagRequest tierTagRequest = td != null
                ? new TierTagRequest(td.getId(), td.getName(), td.getTier())
                : null;

        Integer blindNum = journal.getBlind();
        String blind = (blindNum != null && blindNum >= 0 && blindNum < blindLabel.length)
                ? blindLabel[blindNum]
                : "unknown";


        return new JournalRequest(
                journal.getId(),
                journal.getTitle(),
                journal.getLink(),
                journal.getJournalSupplementEffects().stream()
                        .map(JournalEffectRequest::fromEntity)
                        .toList(),
                journal.getJournalSupplementSideEffects().stream()
                        .map(JournalSideEffectRequest::fromEntity)
                        .toList(),
                journal.getSummary(),
                tierTagRequest,
                blind,
                journal.getParallel(),
                new DurationRequest(journal.getDuration_value(), journal.getDuration_unit(), journal.getDuration_days()),
                journal.getParticipants(),
                journal.getDate()
        );
    }

}