package com.example.examine.entity.JournalSupplementEffect;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class JournalSupplementSideEffectId implements Serializable,JSEId {

    private Long journalId;
    private Long supplementId;
    private Long sideEffectTagId;

    // 기본 생성자
    public JournalSupplementSideEffectId() {}

    // 필드 초기화 생성자
    public JournalSupplementSideEffectId(Long journalId, Long supplementId, Long effectTagId) {
        this.journalId = journalId;
        this.supplementId = supplementId;
        this.sideEffectTagId = sideEffectTagId;
    }

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
    public Long getSupplementId() {
        return supplementId;
    }

    @Override
    public void setSupplementId(Long SupplementId) {
        this.supplementId = SupplementId;
    }

    @Override
    public Long getEffectId() {
        return sideEffectTagId;
    }

    @Override
    public void setEffectId(Long sideEffectTagId) {
        this.sideEffectTagId = sideEffectTagId;
    }

    // equals
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JournalSupplementSideEffectId)) return false;
        JournalSupplementSideEffectId that = (JournalSupplementSideEffectId) o;
        return Objects.equals(journalId, that.journalId)
                && Objects.equals(supplementId, that.supplementId)
                && Objects.equals(sideEffectTagId, that.sideEffectTagId);
    }

    // hashCode
    @Override
    public int hashCode() {
        return Objects.hash(journalId, supplementId, sideEffectTagId);
    }
}
