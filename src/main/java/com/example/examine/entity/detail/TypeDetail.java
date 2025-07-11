package com.example.examine.entity.detail;

import com.example.examine.entity.Tag.Effect.EffectTag;
import com.example.examine.entity.Tag.Tag;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.entity.Tag.TypeTag;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "type_detail")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TypeDetail extends EntityTime implements Detail {

    @Id
    private Long typeId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "type_id")
    private TypeTag typeTag;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String overview;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String intro;

    @Override
    public Long getId() {
        return typeId;
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
        return typeTag;
    }

    @Override
    public void setTag(Tag tag) {
        this.typeTag= (TypeTag) tag;
    }
}
