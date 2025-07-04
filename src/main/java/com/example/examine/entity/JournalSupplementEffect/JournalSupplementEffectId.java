package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.SupplementEffect.SEId;
import com.example.examine.entity.SupplementEffect.SupplementEffectId;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class JournalSupplementEffectId implements Serializable,JSEId {

    private Long journalId;

    @Embedded
    private SupplementEffectId supplementEffectId;

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
    public SupplementEffectId getSEId() {
        return supplementEffectId;
    }

    @Override
    public void setSEId(SEId SEId) {
        this.supplementEffectId = (SupplementEffectId)SEId;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalSupplementEffectId)) return false;
        JournalSupplementEffectId that = (JournalSupplementEffectId) o;
        return Objects.equals(journalId, that.journalId)
                && Objects.equals(supplementEffectId, that.supplementEffectId);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(journalId, supplementEffectId);
    }


}
