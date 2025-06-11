package com.example.examine.dto;

import com.example.examine.entity.*;
import com.example.examine.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.Set;

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

    public static Journal toEntity(JournalRequest dto,
                                   TrialDesignRepository trialDesignRepo,
                                   SupplementRepository supplementRepo,
                                   EffectTagRepository effectRepo,
                                   SideEffectTagRepository sideEffectRepo,
                                   JournalSupplementEffectRepository journalSupplementEffectRepo,
                                   JournalSupplementSideEffectRepository journalSupplementSideEffectRepo) {
        Journal journal = new Journal();
        updateEntity(journal, dto, trialDesignRepo, supplementRepo, effectRepo, sideEffectRepo, journalSupplementEffectRepo, journalSupplementSideEffectRepo);
        return journal;
    }


    public static void updateEntity(Journal journal,
                                    JournalRequest dto,
                                    TrialDesignRepository trialDesignRepo,
                                    SupplementRepository supplementRepo,
                                    EffectTagRepository effectRepo,
                                    SideEffectTagRepository sideEffectRepo,
                                    JournalSupplementEffectRepository journalSupplementEffectRepo,
                                    JournalSupplementSideEffectRepository journalSupplementSideEffectRepo) {

        journal.setDuration_value(dto.duration().value());
        journal.setDuration_unit(dto.duration().unit());
        journal.setDuration_days(dto.duration().days());
        journal.setParticipants(dto.participants());
        int blind = -1;
        for (int i = 0; i < blindLabel.length; i++) {
            if (blindLabel[i].equals(dto.blind())) {
                blind = i;
                break;
            }
        }
        if (blind != -1) {
            journal.setBlind(blind);
        } else {
            journal.setBlind(0);
        }


        if (dto.parallel() == null) {
            journal.setParallel(true);
        } else {
            journal.setParallel(dto.parallel());
        }

        // 🔹 TrialDesign (nullable + id null 방지)
        if (dto.trialDesign() != null && dto.trialDesign().id() != null) {
            TrialDesign td = trialDesignRepo.findById(dto.trialDesign().id())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 trialDesign ID: " + dto.trialDesign().id()));
            journal.setTrial_design(td);
        } else {
            journal.setTrial_design(null);
        }

        journal.getJournalSupplementEffects().clear();
        journalSupplementEffectRepo.deleteByJournalId(journal.getId());
        journal.getJournalSupplementSideEffects().clear();
        journalSupplementSideEffectRepo.deleteByJournalId(journal.getId());

        List<JournalSupplementEffect> newEffects = dto.effects() == null ? List.of() :
                dto.effects().stream()
                        .map(e -> JournalEffectRequest.toEntity(e, journal, supplementRepo, effectRepo))
                        .toList();
        journal.getJournalSupplementEffects().addAll(newEffects);

        List<JournalSupplementSideEffect> newSideEffects = dto.sideEffects() == null ?
                new ArrayList<>() :
                dto.sideEffects().stream()
                        .map(e -> JournalSideEffectRequest.toEntity(e, journal, supplementRepo, sideEffectRepo))
                        .toList();
        journal.getJournalSupplementSideEffects().addAll(newSideEffects);
    }
}