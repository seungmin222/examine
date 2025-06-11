package com.example.examine.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "effect_tag")
public class EffectTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    // Getter, Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    @OneToMany(mappedBy = "effectTag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SupplementEffect> supplementMappings = new ArrayList<>();

    public List<SupplementEffect> getSupplementMappings(){ return supplementMappings; }

    public void setSupplementMappings(List<SupplementEffect> supplementMappings) { this.supplementMappings = supplementMappings; }
}