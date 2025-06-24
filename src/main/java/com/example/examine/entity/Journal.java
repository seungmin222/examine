package com.example.examine.entity;

import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.service.EntityService.JournalService;
import com.example.examine.service.util.CalculateScore;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal")
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(unique = true)
    private String link;

    @Column(nullable = true)
    private String summary;

    @ManyToOne
    @JoinColumn(name = "trial_design_id")
    private TrialDesign trialDesign; // 이미 nullable (nullable=true가 default)

    @Column(nullable = true)
    private Integer durationValue;

    @Column(nullable = true)
    private String durationUnit;

    @Column(nullable = true)
    private Integer durationDays;

    @Column(nullable = true)
    private Integer participants;

    @Column(nullable = true)
    private LocalDate date;

    @Column(nullable = true)
    private Integer blind;

    @Column(nullable = true)
    private Boolean parallel;

    @Column(nullable = true)
    private BigDecimal score;

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalSupplementEffect> journalSupplementEffects = new ArrayList<>();

    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalSupplementSideEffect> journalSupplementSideEffects = new ArrayList<>();

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public TrialDesign getTrialDesign() { return trialDesign;}
    public void setTrialDesign(TrialDesign trialDesign) {
        this.trialDesign = trialDesign;
    }

    public String getDurationUnit() { return durationUnit; }
    public void setDurationUnit(String durationUnit) { this.durationUnit = durationUnit; }

    public Integer getParticipants() { return participants; }
    public void setParticipants(Integer participants) { this.participants = participants; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getDurationValue() {
        return durationValue;
    }

    public void setDurationValue(Integer durationValue) {
        this.durationValue = durationValue;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays() {
        this.durationDays = JournalService.toDays(this.durationValue, this.durationUnit);
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

    public BigDecimal getScore() {
        return score;
    }

    public void setScore() {
        this.score = CalculateScore.calculateJournalScore(participants,durationDays,trialDesign.getName(),blind);
    }
}
