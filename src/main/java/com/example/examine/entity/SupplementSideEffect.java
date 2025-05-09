package com.example.examine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;


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

    private String grade;

    public SupplementSideEffect() {}

    public SupplementSideEffect(Supplement supplement, SideEffectTag sideEffectTag, String grade) {
        this.supplement = supplement;
        this.sideEffectTag = sideEffectTag;
        this.grade = grade;
        this.id = new SupplementSideEffectId(supplement.getId(), sideEffectTag.getId());
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

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}

