package com.example.examine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import com.example.examine.service.util.calculateScore;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "supplement_effect")
public class SupplementEffect {

    @EmbeddedId
    private SupplementEffectId id = new SupplementEffectId();

    @ManyToOne
    @MapsId("supplementId")
    @JoinColumn(name = "supplement_id")
    @JsonIgnore
    private Supplement supplement;

    @ManyToOne
    @MapsId("effectTagId")
    @JoinColumn(name = "effect_tag_id")
    private EffectTag effectTag;

    private String tier;

        private BigDecimal totalScore;      // ∑(score × participants)
        private Integer totalParticipants;     // ∑(participants)
    private BigDecimal finalScore;         // totalScoreSum / totalParticipants


    public SupplementEffect() {}

    public SupplementEffect(Supplement supplement, EffectTag effectTag) {
        this.supplement = supplement;
        this.effectTag = effectTag;
        this.id = new SupplementEffectId(supplement.getId(), effectTag.getId());
        this.totalScore = BigDecimal.ZERO;
        this.finalScore = BigDecimal.ZERO;
        this.totalParticipants = 0;
    }

    // getter/setter

    public SupplementEffectId getId() {
        return id;
    }

    public void setId(SupplementEffectId id) {
        this.id = id;
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

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
        this.tier = calculateScore.calculateTier(finalScore);
    }
}

