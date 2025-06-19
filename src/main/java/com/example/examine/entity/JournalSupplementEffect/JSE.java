package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.Effect.Effect;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Supplement;

import java.math.BigDecimal;

public interface JSE {
    JSEId getId();
    void setId(JSEId id);

    Journal getJournal();
    void setJournal(Journal journal);

    Supplement getSupplement();
    void setSupplement(Supplement supplement);

    Effect getEffect();
    void setEffect(Effect effect);

    BigDecimal getSize();
    void setSize(BigDecimal size);

    BigDecimal getScore();
    void setScore();
}
