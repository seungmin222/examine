package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.Effect.Effect;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.EntityTime;
import com.example.examine.entity.Journal;
import com.example.examine.entity.Supplement;
import com.example.examine.service.util.CalculateScore;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Entity
@Table(name = "journal_supplement_effect")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class JournalSupplementEffect extends EntityTime implements JSE {

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
    @Override
    public JournalSupplementEffectId getId() {
        return id;
    }

    @Override
    public void setId(JSEId id) {
        this.id = (JournalSupplementEffectId) id;
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
    public EffectTag getEffect() {
        return effectTag;
    }

    @Override
    public void setEffect(Effect effect) {
        this.effectTag = (EffectTag)effect;
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

