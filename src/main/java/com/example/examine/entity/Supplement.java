package com.example.examine.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "supplement", uniqueConstraints = @UniqueConstraint(columnNames = {"korName", "engName"}))
public class Supplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String korName;

    @Column(nullable = false)
    private String engName;

    private String dosage;

    private BigDecimal cost;

    @ManyToMany
    @JoinTable(
            name = "supplement_type",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "type_tag_id")
    )
    private List<TypeTag> types = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementEffect> effectMappings = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementSideEffect> sideEffectMappings = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "supplement_pubmed",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "pubmed_id")
    )
    @JsonIgnore
    private List<Pubmed> pubmeds = new ArrayList<>();

 // detail 클래스를 supplement 엔티티에 직접 추가하면 자원낭비
 // detail에서 id 받는게 적절

    public List<Pubmed> getPubmeds() {
        return pubmeds;
    }

    public void setPubmeds(List<Pubmed> pubmeds) {
        this.pubmeds = pubmeds;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKorName() {
        return korName;
    }

    public void setKorName(String korName) {
        this.korName = korName;
    }

    public String getEngName() {
        return engName;
    }

    public void setEngName(String engName) {
        this.engName = engName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public List<TypeTag> getTypes() {
        return types;
    }

    public void setTypes(List<TypeTag> types) {
        this.types = types;
    }

    public List<SupplementEffect> getEffects() {
        return effectMappings;
    }

    public void setEffects(List<SupplementEffect> effectMappings) {
        this.effectMappings = effectMappings;
    }

    public List<SupplementSideEffect> getSideEffects() {
        return sideEffectMappings;
    }

    public void setSideEffects(List<SupplementSideEffect> sideEffectMappings) {this.sideEffectMappings = sideEffectMappings;}
}
