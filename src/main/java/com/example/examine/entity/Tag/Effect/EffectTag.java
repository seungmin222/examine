package com.example.examine.entity.Tag.Effect;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.examine.entity.extend.EntityTime;
import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.Tag.Tag;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "effect_tag")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EffectTag extends EntityTime implements Effect, Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String korName;

    @Column(unique = true)
    private String engName;

    @Builder.Default
    @OneToMany(mappedBy = "effectTag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SupplementEffect> se = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private String tier = "D";

    // Getter, Setter
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getKorName() {
        return korName;
    }

    @Override
    public void setKorName(String korName) {
        this.korName = korName;
    }

    @Override
    public String getEngName() {
        return engName;
    }

    @Override
    public void setEngName(String engName) {
        this.engName = engName;
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
    public List<SE> getSE() {
        return se.stream()
                .map(e -> (SE) e)
                .collect(Collectors.toList());
    }


    @Override
    public void setSE(List<SE> se) {
        List<SupplementEffect> casted = new ArrayList<>();
        for (SE item : se) {
            casted.add((SupplementEffect) item); // ⚠️ 다운캐스팅이므로 타입이 보장돼야 함
        }
        this.se = casted;
    }
}