package com.example.examine.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.examine.service.util.calculateScore;

import java.math.BigDecimal;


@Entity
@Table(name = "supplement_side_effect")
public class SupplementSideEffect {

    @EmbeddedId
    private SupplementSideEffectId id = new SupplementSideEffectId();

    @ManyToOne
    @MapsId("supplementId")
    @JoinColumn(name = "supplement_id")
    @JsonIgnore
    private Supplement supplement;

    @ManyToOne
    @MapsId("sideEffectTagId")
    @JoinColumn(name = "side_effect_tag_id")
    private SideEffectTag sideEffectTag;

    private String tier;

    private BigDecimal totalScore;      // ∑(score × participants)
    private Integer totalParticipants;     // ∑(participants)
    private BigDecimal finalScore;         // totalScoreSum / totalParticipants


    public SupplementSideEffect() {}

    public SupplementSideEffect(Supplement supplement, SideEffectTag sideEffectTag) {
        this.supplement = supplement;
        this.sideEffectTag = sideEffectTag;
        this.id = new SupplementSideEffectId(supplement.getId(), sideEffectTag.getId());
        this.totalScore = BigDecimal.ZERO;
        this.finalScore = BigDecimal.ZERO;
        this.totalParticipants = 0;
    }

    // getter/setter

    public SupplementSideEffectId getId() {
        return id;
    }

    public void setId(SupplementSideEffectId id) {
        this.id = id;
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

