package com.example.examine.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "journal")
public class Journal {

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

    private Integer duration_value;

    private String duration_unit;

    private Integer duration_days;

    private Integer participants;

    private LocalDate date;

    private Integer blind;

    private Boolean parallel;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalSupplementEffect> journalSupplementEffects = new ArrayList<>();

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalSupplementSideEffect> journalSupplementSideEffects = new ArrayList<>();

    public void removeJournalSupplementEffect(JournalSupplementEffect effect) {
        this.journalSupplementEffects.remove(effect);
        effect.setJournal(null); // 🔹 양방향 연관관계 끊어주기
    }

    public void removeJournalSupplementSideEffect(JournalSupplementSideEffect sideEffect) {
        this.journalSupplementSideEffects.remove(sideEffect);
        sideEffect.setJournal(null); // 🔹 양방향 연관관계 끊어주기
    }

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

    public String getDuration_unit() { return duration_unit; }
    public void setDuration_unit(String duration_unit) { this.duration_unit = duration_unit; }

    public Integer getParticipants() { return participants; }
    public void setParticipants(Integer participants) { this.participants = participants; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getDuration_value() {
        return duration_value;
    }

    public void setDuration_value(Integer duration_value) {
        this.duration_value = duration_value;
    }

    public Integer getDuration_days() {
        return duration_days;
    }

    public void setDuration_days(Integer duration_days) {
        this.duration_days = duration_days;
    }

    public List<JournalSupplementEffect> getJournalSupplementEffects() {
        return journalSupplementEffects;
    }

    public void setJournalSupplementEffects(List<JournalSupplementEffect> journalSupplementEffects) {
        this.journalSupplementEffects = journalSupplementEffects;
    }

    public List<JournalSupplementSideEffect> getJournalSupplementSideEffects() {
        return journalSupplementSideEffects;
    }

    public void setJournalSupplementSideEffects(List<JournalSupplementSideEffect> journalSupplementSideEffects) {
        this.journalSupplementSideEffects = journalSupplementSideEffects;
    }

    public Integer getBlind() {
        return blind;
    }

    public void setBlind(Integer blind) {
        this.blind = blind;
    }


    public Boolean getParallel() {
        return parallel;
    }

    public void setParallel(Boolean parallel) {
        this.parallel = parallel;
    }
}
