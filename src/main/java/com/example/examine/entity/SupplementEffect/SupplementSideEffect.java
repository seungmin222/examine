package com.example.examine.entity.SupplementEffect;

import com.example.examine.entity.Effect.Effect;
import com.example.examine.entity.Effect.SideEffectTag;
import com.example.examine.entity.EntityTime;
import com.example.examine.entity.Supplement;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.examine.service.util.CalculateScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Entity
@Table(name = "supplement_side_effect")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SupplementSideEffect extends EntityTime implements SE {

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

    @Column(name = "total_score", nullable = false)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Column(name = "total_participants", nullable = false)
    private Integer totalParticipants = 0;

    @Column(name = "final_score", nullable = false)
    private BigDecimal finalScore = BigDecimal.ZERO;

    public SupplementSideEffect(Supplement supplement, SideEffectTag sideEffectTag) {
        this.supplement = supplement;
        this.sideEffectTag = sideEffectTag;
        this.id = new SupplementSideEffectId(supplement.getId(), sideEffectTag.getId());
        this.totalScore = BigDecimal.ZERO;
        this.finalScore = BigDecimal.ZERO;
        this.totalParticipants = 0;
    }

    // getter/setter
    @Override
    public SupplementSideEffectId getId() {
        return id;
    }

    @Override
    public void setId(SEId id) {
        this.id = (SupplementSideEffectId) id;
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

