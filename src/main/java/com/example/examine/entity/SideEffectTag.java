package com.example.examine.entity;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "side_effect_tag")
public class SideEffectTag {
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

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "sideEffectTag", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SupplementSideEffect> supplementMappings = new ArrayList<>();

    public List<SupplementSideEffect> getSupplementMappings(){ return supplementMappings; }

    public void setSupplementMappings(List<SupplementSideEffect> supplementMappings) {
        this.supplementMappings = supplementMappings;
    }
}
