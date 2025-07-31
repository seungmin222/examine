package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.Tag.Effect.Effect;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Tag.Supplement;

import java.math.BigDecimal;

public interface JSE<I extends JSEId> {

    JSEId getId();
    void setId(JSEId id);

    Journal getJournal();
    void setJournal(Journal journal);

    SE getSE();

    void setSE(SE se);

    BigDecimal getCohenD();
    void setCohenD(BigDecimal cohenD);

    BigDecimal getPearsonR();
    void setPearsonR(BigDecimal pearsonR);

    BigDecimal getPValue();
    void setPValue(BigDecimal pValue);

    BigDecimal getScore();
    void setScore();

    String getTier();
    void setTier(String tier);

    public Integer getParticipants();
    public void setParticipants(Integer participants);
}
