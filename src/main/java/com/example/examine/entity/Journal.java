package com.example.examine.entity;

import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.service.EntityService.JournalService;
import com.example.examine.service.util.CalculateScore;
import com.example.examine.service.util.EnumService;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(
        name = "journal",
        uniqueConstraints = @UniqueConstraint(columnNames = {"site_type", "site_journal_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Journal extends EntityTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING) // ✅ enum 이름 그대로 저장
    @Column(name = "site_type", nullable = false, columnDefinition = "VARCHAR(50)")
    private EnumService.JournalSiteType siteType;

    @Column(name = "site_journal_id", nullable = false)
    private String siteJournalId;

    @Column(nullable = true)
    private String summary;

    @ManyToOne
    @JoinColumn(name = "trial_design_id")
    private TrialDesign trialDesign;

    @Column(nullable = true)
    private Integer durationValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", nullable = true)
    private EnumService.DurationUnit durationUnit;

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

    @Builder.Default
    @Column(nullable = false)
    private String tier = "D";

    @Builder.Default
    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalSupplementEffect> journalSupplementEffects = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "journal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<JournalSupplementSideEffect> journalSupplementSideEffects = new ArrayList<>();

    public void setDurationDays() {
        this.durationDays = JournalService.toDays(this.durationValue, this.durationUnit);
    }

    public void setScore() {
        this.score = CalculateScore.calculateJournalScore(participants,durationDays,trialDesign,blind);
    }
}
