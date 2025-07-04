package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.SupplementEffect.SEId;
import com.example.examine.entity.SupplementEffect.SupplementEffectId;

public interface JSEId {
    Long getJournalId();
    void setJournalId(Long journalId);

    SEId getSEId();
    void setSEId(SEId SEId);
}
