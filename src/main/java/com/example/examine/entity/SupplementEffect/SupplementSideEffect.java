package com.example.examine.entity.SupplementEffect;

import com.example.examine.entity.Tag.Effect.Effect;
import com.example.examine.entity.Tag.Effect.SideEffectTag;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.entity.Tag.Supplement;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.examine.service.util.CalculateScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.RoundingMode;


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

    @Column(name = "supplement_kor_name")
    private String supplementKorName;

    @Column(name = "supplement_eng_name")
    private String supplementEngName;

    @Column(name = "effect_kor_name")
    private String effectKorName;

    @Column(name = "effect_eng_name")
    private String effectEngName;

    // 이름 동기화
    @PrePersist
    @PreUpdate
    public void syncNames() {
        this.supplementKorName = supplement.getKorName();
        this.supplementEngName = supplement.getEngName();
        this.effectKorName = sideEffectTag.getKorName();
        this.effectEngName = sideEffectTag.getEngName();
        this.tier = "D";

        this.plusCount = 0;
        this.minusCount = 0;
        this.zeroCount = 0;

        this.plusParticipants = 0;
        this.minusParticipants = 0;
        this.zeroParticipants = 0;
    }

    @Builder.Default
    @Column(nullable = false)
    private String tier = "D";

    @Builder.Default
    @Column(name = "total_score", nullable = false)
    private BigDecimal totalScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "total_participants", nullable = false)
    private Integer totalParticipants = 0;

    @Builder.Default
    @Column(name = "final_score", nullable = false)
    private BigDecimal finalScore = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "plus_count", nullable = false)
    private Integer plusCount = 0;

    @Builder.Default
    @Column(name = "minus_count", nullable = false)
    private Integer minusCount = 0;

    @Builder.Default
    @Column(name = "zero_count", nullable = false)
    private Integer zeroCount = 0;

    @Builder.Default
    @Column(name = "plus_participants", nullable = false)
    private Integer plusParticipants = 0;

    @Builder.Default
    @Column(name = "minus_participants", nullable = false)
    private Integer minusParticipants = 0;

    @Builder.Default
    @Column(name = "zero_participants", nullable = false)
    private Integer zeroParticipants = 0;
    public SupplementSideEffect(Supplement supplement, SideEffectTag sideEffectTag) {
        this.supplement = supplement;
        this.sideEffectTag = sideEffectTag;
        this.id = new SupplementSideEffectId(supplement.getId(), sideEffectTag.getId());
        this.totalScore = BigDecimal.ZERO;
        this.finalScore = BigDecimal.ZERO;
        this.totalParticipants = 0;

        this.tier = "D";

        this.plusCount = 0;
        this.minusCount = 0;
        this.zeroCount = 0;

        this.plusParticipants = 0;
        this.minusParticipants = 0;
        this.zeroParticipants = 0;
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
    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore =  finalScore;
    }
    @Override
    public String getSupplementKorName() {
        return supplementKorName;
    }

    @Override
    public void setSupplementKorName(String name) {
        this.supplementKorName = name;
    }

    @Override
    public String getSupplementEngName() {
        return supplementEngName;
    }

    @Override
    public void setSupplementEngName(String name) {
        this.supplementEngName = name;
    }

    @Override
    public String getEffectKorName() {
        return effectKorName;
    }

    @Override
    public void setEffectKorName(String name) {
        this.effectKorName = name;
    }

    @Override
    public String getEffectEngName() {
        return effectEngName;
    }

    @Override
    public void setEffectEngName(String name) {
        this.effectEngName = name;
    }

    @Override
    public Integer getPlusCount() {
        return plusCount;
    }

    @Override
    public void setPlusCount(Integer plusCount) {
        this.plusCount = plusCount;
    }

    @Override
    public Integer getMinusCount() {
        return minusCount;
    }

    @Override
    public void setMinusCount(Integer minusCount) {
        this.minusCount = minusCount;
    }

    @Override
    public Integer getZeroCount() {
        return zeroCount;
    }

    @Override
    public void setZeroCount(Integer zeroCount) {
        this.zeroCount = zeroCount;
    }

// ✅ participants 관련 추가

    @Override
    public Integer getPlusParticipants() {
        return plusParticipants;
    }

    @Override
    public void setPlusParticipants(Integer plusParticipants) {
        this.plusParticipants = plusParticipants;
    }

    @Override
    public Integer getMinusParticipants() {
        return minusParticipants;
    }

    @Override
    public void setMinusParticipants(Integer minusParticipants) {
        this.minusParticipants = minusParticipants;
    }

    @Override
    public Integer getZeroParticipants() {
        return zeroParticipants;
    }

    @Override
    public void setZeroParticipants(Integer zeroParticipants) {
        this.zeroParticipants = zeroParticipants;
    }

}

