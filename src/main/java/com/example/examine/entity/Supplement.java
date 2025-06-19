package com.example.examine.entity;

import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "supplement", uniqueConstraints = @UniqueConstraint(columnNames = {"korName", "engName"}))
public class Supplement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true , nullable = false)
    private String korName;

    @Column(unique = true, nullable = false)
    private String engName;

    private String dosage;

    private BigDecimal cost;

    private LocalDateTime date;

    @ManyToMany
    @JoinTable(
            name = "supplement_type",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "type_tag_id")
    )
    private List<TypeTag> types = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementEffect> effects = new ArrayList<>();

    @OneToMany(mappedBy = "supplement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementSideEffect> sideEffects = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "supplement_journal",
            joinColumns = @JoinColumn(name = "supplement_id"),
            inverseJoinColumns = @JoinColumn(name = "journal_id")
    )
    @JsonIgnore
    private List<Journal> journals = new ArrayList<>();

 // detail 클래스를 supplement 엔티티에 직접 추가하면 자원낭비
 // detail에서 id 받는게 적절

    public List<Journal> getJournals() {
        return journals;
    }

    public void setJournals(List<Journal> journals) {
        this.journals = journals;
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
        return effects;
    }

    public void setEffects(List<SupplementEffect> effectMappings) {
        this.effects = effectMappings;
    }

    public List<SupplementSideEffect> getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(List<SupplementSideEffect> sideEffectMappings) {this.sideEffects = sideEffectMappings;}

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
