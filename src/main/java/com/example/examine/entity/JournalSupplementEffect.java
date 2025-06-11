package com.example.examine.entity;

import com.example.examine.service.util.calculateScore;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;


@Entity
@Table(name = "journal_supplement_effect")
public class JournalSupplementEffect {

    @EmbeddedId
    private JournalSupplementEffectId id = new JournalSupplementEffectId();

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
    @MapsId("effectTagId")
    @JoinColumn(name = "effect_tag_id")
    private EffectTag effectTag;

    @Column(precision = 5, scale = 3)
    private BigDecimal size;

    @Column(precision = 8, scale = 4)
    private BigDecimal score;

    public JournalSupplementEffect() {}

    public JournalSupplementEffect(Journal journal
            , Supplement supplement
            , EffectTag effectTag
            , BigDecimal size) {
        this.journal = journal;
        this.supplement = supplement;
        this.effectTag = effectTag;
        this.size = size;
        this.id = new JournalSupplementEffectId(journal.getId(),supplement.getId(), effectTag.getId());
    }

    // getter/setter

    public JournalSupplementEffectId getId() {
        return id;
    }

    public void setId(JournalSupplementEffectId id) {
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

    public EffectTag getEffectTag() {
        return effectTag;
    }

    public void setEffectTag(EffectTag effectTag) {
        this.effectTag = effectTag;
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

    public void setScore() {
        if (this.journal == null || this.size == null) {
            this.score = null;
            return;
        }

        Integer participants = this.journal.getParticipants();
        Integer duration = this.journal.getDuration_days();

        if (participants == null || duration == null) {
            this.score = null;
            return;
        }


        BigDecimal strength = this.size;
        String design = this.getJournal().getTrial_design().getName();
        Integer blind = this.getJournal().getBlind();

        this.score = calculateScore.calculateScore(strength, participants, duration, design, blind); // 정밀도 조절
    }

}

