package com.example.examine.dto.response;
import com.example.examine.entity.Journal;
import com.example.examine.entity.TrialDesign;

import java.time.LocalDate;
import java.util.List;

public record JournalResponse(
        Long id,
        String title,
        String link,
        List<JSEResponse> effects,
        List<JSEResponse> sideEffects,
        String summary,
        TierTagResponse trialDesign,
        String blind,
        Boolean parallel,
        DurationResponse duration,
        Integer participants,
        LocalDate date
) {
    private static final String[] blindLabel = {
            "open-label", "single-blind", "double-blind"
    };

    public static JournalResponse fromEntity(Journal journal) {
        TrialDesign td = journal.getTrialDesign();
        TierTagResponse tierTagResponse = td != null
                ? new TierTagResponse(td.getId(), td.getName(), td.getTier())
                : null;

        Integer blindNum = journal.getBlind();
        String blind = (blindNum != null && blindNum >= 0 && blindNum < blindLabel.length)
                ? blindLabel[blindNum]
                : "unknown";


        return new JournalResponse(
                journal.getId(),
                journal.getTitle(),
                journal.getLink(),
                journal.getJournalSupplementEffects().stream()
                        .map(JSEResponse::fromEntity)
                        .toList(),
                journal.getJournalSupplementSideEffects().stream()
                        .map(JSEResponse::fromEntity)
                        .toList(),
                journal.getSummary(),
                tierTagResponse,
                blind,
                journal.getParallel(),
                new DurationResponse(journal.getDurationValue(), journal.getDurationUnit(), journal.getDurationDays()),
                journal.getParticipants(),
                journal.getDate()
        );
    }
}
