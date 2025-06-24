package com.example.examine.entity.SupplementEffect;

import com.example.examine.entity.Effect.Effect;
import com.example.examine.entity.Effect.EffectTag;
import com.example.examine.entity.Supplement;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.example.examine.service.util.CalculateScore;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "supplement_effect")
public class SupplementEffect implements SE {

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

    @Column(name = "total_score", nullable = false)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(name = "total_participants", nullable = false)
    private Integer totalParticipants = 0;

    @Column(name = "final_score", nullable = false)
    private BigDecimal finalScore = BigDecimal.ZERO;       // totalScoreSum / totalParticipants


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
    @Override
    public SupplementEffectId getId() {
        return id;
    }

    @Override
    public void setId(SEId id) {
        this.id = (SupplementEffectId) id;
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
        this.effectTag = (EffectTag) effect;
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
    public BigDecimal getTotalScore() {
        return totalScore;
    }

    @Override
    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    @Override
    public Integer getTotalParticipants() {
        return totalParticipants;
    }

    @Override
    public void setTotalParticipants(Integer totalParticipants) {
        this.totalParticipants = totalParticipants;
    }

    @Override
    public BigDecimal getFinalScore() {
        return finalScore;
    }

    @Override
    public void setFinalScore() {
        if(this.totalParticipants==0){
            this.finalScore = BigDecimal.ZERO;
        }
        else {
            this.finalScore = this.totalScore.divide(
                    BigDecimal.valueOf(this.totalParticipants),
                    4,  // 소수점 4자리
                    RoundingMode.HALF_UP
            );
        }
        this.tier = CalculateScore.calculateTier(finalScore);
    }
}

