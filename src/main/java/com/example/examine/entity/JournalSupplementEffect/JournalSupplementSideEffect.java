package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.Effect.Effect;
import com.example.examine.entity.Effect.SideEffectTag;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Supplement;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import com.example.examine.service.util.CalculateScore;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Entity
@Table(name = "journal_supplement_side_effect")
public class JournalSupplementSideEffect implements JSE {

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

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
    @Override
    public JournalSupplementSideEffectId getId() {
        return id;
    }

    @Override
    public void setId(
            JSEId id) {
        this.id = (JournalSupplementSideEffectId) id;
    }

    @Override
    public Journal getJournal() {
        return journal;
    }

    @Override
    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    @Override
    public Supplement getSupplement() {
        return supplement;
    }

    @Override
    public void setSupplement(Supplement supplement) {
        this.supplement = supplement;
    }

    @Override
    public SideEffectTag getEffect() {
        return sideEffectTag;
    }

    @Override
    public void setEffect(Effect effect) {
        this.sideEffectTag = (SideEffectTag) effect;
    }

    @Override
    public BigDecimal getSize() {
        return size;
    }

    @Override
    public void setSize(BigDecimal size) {
        this.size = size;
    }

    @Override
    public BigDecimal getScore() {
        return score;
    }

    @Override
    public void setScore() {
        this.score = CalculateScore.calculateJournalSupplementScore(this.size, this.journal.getScore());
        return;
    }
}

