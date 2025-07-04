package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.SupplementEffect.SEId;
import com.example.examine.entity.SupplementEffect.SupplementSideEffectId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class JournalSupplementSideEffectId implements Serializable,JSEId {

    private Long journalId;

    @Embedded
    private SupplementSideEffectId supplementSideEffectId;

    // Getter & Setter
    @Override
    public Long getJournalId() {
        return journalId;
    }

    @Override
    public void setJournalId(Long journalId) {
        this.journalId = journalId;
    }

    @Override
    public SupplementSideEffectId getSEId() {
        return supplementSideEffectId;
    }

    @Override
    public void setSEId(SEId seId) {
        this.supplementSideEffectId = (SupplementSideEffectId)seId;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalSupplementSideEffectId)) return false;
        JournalSupplementSideEffectId that = (JournalSupplementSideEffectId) o;
        return Objects.equals(journalId, that.journalId)
                && Objects.equals(supplementSideEffectId, that.supplementSideEffectId);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(journalId, supplementSideEffectId);
    }


}
