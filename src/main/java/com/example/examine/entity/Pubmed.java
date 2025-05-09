package com.example.examine.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pubmed")
public class Pubmed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String link;

    @Column(columnDefinition = "TEXT") // 긴 요약 대응
    private String summary;

    @ManyToOne
    @JoinColumn(name = "trial_design_id")
    private TrialDesign trial_design;

    private String trial_length;

    private Integer participants;


    @ManyToMany
    @JoinTable(
            name = "supplement_pubmed",
            joinColumns = @JoinColumn(name = "pubmed_id"),
            inverseJoinColumns = @JoinColumn(name = "supplement_id")
    )
    private List<Supplement> supplements = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "effect_pubmed",
            joinColumns = @JoinColumn(name = "pubmed_id"),
            inverseJoinColumns = @JoinColumn(name = "effect_tag_id")
    )
    private List<EffectTag> effects = new ArrayList<>();
    @ManyToMany
    @JoinTable(
            name = "side_effect_pubmed",
            joinColumns = @JoinColumn(name = "pubmed_id"),
            inverseJoinColumns = @JoinColumn(name = "side_effect_tag_id")
    )
    private List<SideEffectTag> sideEffects = new ArrayList<>();

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public TrialDesign getTrial_design() { return trial_design;}
    public void setTrial_design(TrialDesign trial_design) {
        this.trial_design = trial_design;
    }

    public String getTrial_length() { return trial_length; }
    public void setTrial_length(String trial_length) { this.trial_length = trial_length; }

    public Integer getParticipants() { return participants; }
    public void setParticipants(Integer participants) { this.participants = participants; }

    public List<Supplement> getSupplements() { return supplements; }
    public void setSupplements(List<Supplement> supplements) { this.supplements = supplements; }

    public List<EffectTag> getEffects() { return effects; }
    public void setEffects(List<EffectTag> effects) { this.effects = effects; }

    public List<SideEffectTag> getSideEffects() { return sideEffects; }
    public void setSideEffects(List<SideEffectTag> sideEffects) { this.sideEffects = sideEffects; }
}
