package com.example.examine.entity.JournalSupplementEffect;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JournalSupplementEffectId implements Serializable,JSEId {

    private Long journalId;
    private Long supplementId;
    private Long effectTagId;

    // 기본 생성자
    public JournalSupplementEffectId() {}

    // 필드 초기화 생성자
    public JournalSupplementEffectId(Long journalId, Long supplementId, Long effectTagId) {
        this.journalId = journalId;
        this.supplementId = supplementId;
        this.effectTagId = effectTagId;
    }

    // Getter & Setter
    public Long getJournalId() {
        return journalId;
    }

    public void setJournalId(Long journalId) {
        this.journalId = journalId;
    }

    public Long getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(Long SupplementId) {
        this.supplementId = SupplementId;
    }

    public Long getEffectId() {
        return effectTagId;
    }

    public void setEffectId(Long effectTagId) {
        this.effectTagId = effectTagId;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalSupplementEffectId)) return false;
        JournalSupplementEffectId that = (JournalSupplementEffectId) o;
        return Objects.equals(journalId, that.journalId)
                && Objects.equals(supplementId, that.supplementId)
                && Objects.equals(effectTagId, that.effectTagId);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(journalId, supplementId, effectTagId);
    }
}
