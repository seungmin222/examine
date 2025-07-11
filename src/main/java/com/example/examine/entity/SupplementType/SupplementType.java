package com.example.examine.entity.SupplementType;

import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.Tag.TypeTag;
import com.example.examine.entity.extend.EntityTime;
import jakarta.persistence.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Entity
@Table(name = "supplement_type")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public class SupplementType extends EntityTime{

    @EmbeddedId
    private SupplementTypeId id = new SupplementTypeId();

    @ManyToOne
    @MapsId("supplementId")
    @JoinColumn(name = "supplement_id")
    @JsonIgnore
    private Supplement supplement;

    @ManyToOne
    @MapsId("typeTagId")
    @JoinColumn(name = "type_tag_id")
    private TypeTag typeTag;

    @Column(name = "supplement_kor_name")
    private String supplementKorName;

    @Column(name = "supplement_eng_name")
    private String supplementEngName;

    @Column(name = "type_kor_name")
    private String typeKorName;

    @Column(name = "type_eng_name")
    private String typeEngName;

    // 이름 동기화
    @PrePersist
    @PreUpdate
    public void syncNames() {
        this.supplementKorName = supplement.getKorName();
        this.supplementEngName = supplement.getEngName();
        this.typeKorName = typeTag.getKorName();
        this.typeEngName = typeTag.getEngName();
    }

    public SupplementType(Supplement supplement, TypeTag typeTag) {
        this.supplement = supplement;
        this.typeTag = typeTag;
        this.id = new SupplementTypeId(supplement.getId(), typeTag.getId());
    }
}

