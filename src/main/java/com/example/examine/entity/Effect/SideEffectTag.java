package com.example.examine.entity.Effect;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.examine.entity.EntityTime;
import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "side_effect_tag")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class SideEffectTag extends EntityTime implements Effect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @OneToMany(mappedBy = "sideEffectTag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SupplementSideEffect> se = new ArrayList<>();

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
        List<SupplementSideEffect> casted = new ArrayList<>();
        for (SE item : se) {
            casted.add((SupplementSideEffect) item); // ⚠️ 다운캐스팅이므로 타입이 보장돼야 함
        }
        this.se = casted;
    }
}
