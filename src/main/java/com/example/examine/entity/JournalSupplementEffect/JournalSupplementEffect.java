package com.example.examine.entity.JournalSupplementEffect;

import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.entity.Journal;
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
    @MapsId("supplementEffectId")
    @JoinColumns({
            @JoinColumn(name = "supplement_id", referencedColumnName = "supplement_id"),
            @JoinColumn(name = "effect_tag_id", referencedColumnName = "effect_tag_id")
    })
    private SupplementEffect SE;

    @Column(name = "cohen_d", precision = 5, scale = 3)
    private BigDecimal cohenD;

    @Column(name = "pearson_r", precision = 5, scale = 3)
    private BigDecimal pearsonR;

    @Column(name = "p_value", precision = 6, scale = 5)
    private BigDecimal pValue;

    @Column(precision = 8, scale = 4)
    private BigDecimal score;

    @Column(length = 2, nullable = false)
    private String tier = "D";

    private Integer participants;

    public JournalSupplementEffect(Journal journal, SupplementEffect se, BigDecimal d, BigDecimal r, BigDecimal p) {
        this.journal = journal;
        this.SE = se;
        this.cohenD = d;
        this.pearsonR = r;
        this.pValue = p;
        this.id = new JournalSupplementEffectId(journal.getId(), se.getId());
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
    public SupplementEffect getSE() {
        return SE;
    }

    @Override
    public void setSE(SE se) {
        this.SE = (SupplementEffect)se;
    }

    @Override
    public BigDecimal getCohenD() {
        return cohenD;
    }

    @Override
    public void setCohenD(BigDecimal cohenD) {
        this.cohenD = cohenD;
    }

    @Override
    public BigDecimal getPearsonR() {
        return pearsonR;
    }

    @Override
    public void setPearsonR(BigDecimal pearsonR) {
        this.pearsonR = pearsonR;
    }

    @Override
    public BigDecimal getPValue() {
        return pValue;
    }

    @Override
    public void setPValue(BigDecimal pValue) {
        this.pValue = pValue;
    }

    @Override
    public BigDecimal getScore() {
        return score;
    }

    @Override
    public void setScore() {
        this.score = CalculateScore.calculateJournalSupplementScore(this.cohenD, this.pearsonR, this.pValue, this.journal.getScore());
    }
    @Override
    public String getTier() {
        return tier;
    }

    @Override
    public void setTier(String tier) {
        this.tier = tier;
    }

    @Override
    public Integer getParticipants() {
        return participants;
    }

    @Override
    public void setParticipants(Integer participants) {
        this.participants = participants;
    }
}

