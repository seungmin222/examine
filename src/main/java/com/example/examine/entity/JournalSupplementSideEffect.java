package com.example.examine.entity;

import com.example.examine.service.util.calculateScore;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Entity
@Table(name = "journal_supplement_side_effect")
public class JournalSupplementSideEffect {

    @EmbeddedId
    private JournalSupplementSideEffectId id = new JournalSupplementSideEffectId();

    @ManyToOne
    @MapsId("journalId")
    @JoinColumn(name = "journal_id")
    @JsonIgnore
    private Journal journal;

    @ManyToOne
    @MapsId("supplementId")
    @JoinColumn(name = "supplement_id")
    private Supplement supplement;

    @ManyToOne
    @MapsId("sideEffectTagId")
    @JoinColumn(name = "side_effect_tag_id")
    private SideEffectTag sideEffectTag;

    @Column(precision = 5, scale = 3)
    private BigDecimal size;

    @Column(precision = 8, scale = 4)
    private BigDecimal score;

    public JournalSupplementSideEffect() {}

    public JournalSupplementSideEffect(Journal journal,
            Supplement supplement,
            SideEffectTag sideEffectTag,
            BigDecimal size) {
        this.journal = journal;
        this.supplement = supplement;
        this.sideEffectTag = sideEffectTag;
        this.size = size;
        this.id = new JournalSupplementSideEffectId(journal.getId(), supplement.getId(), sideEffectTag.getId());
    }

    // getter/setter

    public JournalSupplementSideEffectId getId() {
        return id;
    }

    public void setId(JournalSupplementSideEffectId id) {
        this.id = id;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Supplement getSupplement() {
        return supplement;
    }

    public void setSupplement(Supplement supplement) {
        this.supplement = supplement;
    }

    public SideEffectTag getSideEffectTag() {
        return sideEffectTag;
    }

    public void setSideEffectTag(SideEffectTag sideEffectTag) {
        this.sideEffectTag = sideEffectTag;
    }

    public BigDecimal getSize() {
        return size;
    }

    public void setSize(BigDecimal size) {
        this.size = size;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(SupplementSideEffect agg, int oldParticipants) {
        if (this.journal == null || this.journal.getScore() == null || this.size == null) {
            this.score = null;
            return;
        }
        int participants = this.journal.getParticipants();
        BigDecimal newScore = calculateScore.calculateJournalSupplementScore(this.size, this.journal.getScore())
                .setScale(4, RoundingMode.HALF_UP);

        // 기존 점수가 있었으면 제거
        if (this.score != null) {
            BigDecimal oldContribution = this.score.multiply(BigDecimal.valueOf(oldParticipants));
            agg.setTotalScore(agg.getTotalScore().subtract(oldContribution));
            agg.setTotalParticipants(agg.getTotalParticipants() - oldParticipants);
        }
        ///  totalscore null 일때 걍 0으로 처리
        // 새 점수 반영
        BigDecimal newContribution = newScore.multiply(BigDecimal.valueOf(participants));
        agg.setTotalScore(agg.getTotalScore().add(newContribution));
        agg.setTotalParticipants(agg.getTotalParticipants() + participants);

        if (agg.getTotalParticipants() > 0) {
            agg.setFinalScore(agg.getTotalScore()
                    .divide(BigDecimal.valueOf(agg.getTotalParticipants()), 4, RoundingMode.HALF_UP));
        } else {
            agg.setFinalScore(BigDecimal.ZERO);
        }

        this.score = newScore;
    }
}

