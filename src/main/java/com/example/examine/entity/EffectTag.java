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

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(mappedBy = "effectTag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplementEffect> supplementMappings = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "effect_pubmed",
            joinColumns = @JoinColumn(name = "effect_tag_id"),
            inverseJoinColumns = @JoinColumn(name = "pubmed_id")
    )
    @JsonIgnore
    private List<Pubmed> pubmeds = new ArrayList<>();
}