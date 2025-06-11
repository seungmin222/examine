package com.example.examine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    public SupplementEffect() {}

    public SupplementEffect(Supplement supplement, EffectTag effectTag, String tier) {
        this.supplement = supplement;
        this.effectTag = effectTag;
        this.tier = tier;
        this.id = new SupplementEffectId(supplement.getId(), effectTag.getId());
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
}

