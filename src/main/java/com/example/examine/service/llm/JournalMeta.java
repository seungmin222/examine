package com.example.examine.service.llm;

import com.example.examine.entity.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementSideEffect;

import java.time.LocalDate;
import java.util.List;

public class JournalMeta {
    private Integer participants;
    private Integer durationDays;
    private Integer blind;
    private boolean parallel;
    private String trialDesign;
    private List<SupplementEffectSize> effects;
    private List<SupplementSideEffectSize> sideEffects;

    public JournalMeta(String title, Integer participants, String summary, LocalDate date) {
        this.participants = participants;
        this.durationDays = durationDays;

    }



}