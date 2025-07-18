package com.example.examine.entity.Tag;

import com.example.examine.entity.detail.SupplementDetail;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.entity.Journal;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import com.example.examine.service.util.EnumService;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "supplement", uniqueConstraints = @UniqueConstraint(columnNames = {"korName", "engName"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Supplement extends EntityTime implements Tag{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true , nullable = false)
    private String korName;

    @Column(unique = true, nullable = false)
    private String engName;

    private BigDecimal dosageValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "dosage_unit")
    private EnumService.DosageUnit dosageUnit;

    private BigDecimal cost;

    private LocalDateTime date;

    @ManyToMany
    @JoinTable(
            name = "supplement_type",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "type_tag_id")
    )

    @Builder.Default
    private List<TypeTag> types = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementEffect> effects = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementSideEffect> sideEffects = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private String tier = "D";

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
        this.korName = this.korName;
    }

    @Override
    public String getEngName() {
        return engName;
    }

    @Override
    public void setEngName(String korName) {
        this.engName = this.engName;
    }

    @Override
    public String getTier() {
        return tier;
    }

    @Override
    public void setTier(String tier) {
        this.tier = this.tier;
    }

    public String getDosageUnit() {
        return dosageUnit != null ? dosageUnit.name().toLowerCase() : null;
    }

    public void setDosageUnit(String str) {
        this.dosageUnit = EnumService.DosageUnit.fromString(str);
    }
}
