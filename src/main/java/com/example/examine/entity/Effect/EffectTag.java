package com.example.examine.entity.Effect;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "effect_tag")
public class EffectTag implements Effect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "effectTag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SupplementEffect> se = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getter, Setter
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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