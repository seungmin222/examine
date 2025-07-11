package com.example.examine.entity.detail;

import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "effect_detail")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class EffectDetail extends EntityTime implements Detail {

    @Id
    private Long effectId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "effect_id")
    private EffectTag effectTag;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String overview;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String intro;

    @Override
    public Long getId() {
        return effectId;
    }

    @Override
    public String getOverview() {
        return overview;
    }

    @Override
    public void setOverview(String overview) {
        this.overview= overview;
    }

    @Override
    public String getIntro() {
        return intro;
    }

    @Override
    public void setIntro(String intro) {
        this.intro= intro;
    }

    @Override
    public Tag getTag() {
        return effectTag;
    }

    @Override
    public void setTag(Tag tag) {
        this.effectTag= (EffectTag) tag;
    }

}

