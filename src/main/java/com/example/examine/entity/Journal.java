package com.example.examine.entity;

import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.entity.Tag.TrialDesign;
import com.example.examine.entity.extend.EntityTime;
import com.example.examine.service.EntityService.JournalService;
import com.example.examine.service.util.CalculateScore;
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
@Table(name = "journal")
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

    @Column(unique = true)
    private String link;

    @Column(nullable = true)
    private String summary;

    @ManyToOne
    @JoinColumn(name = "trial_design_id")
    private TrialDesign trialDesign;

    @Column(nullable = true)
    private Integer durationValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", nullable = true)
    private DurationUnit durationUnit;

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

    public String getDurationUnit() {
        return durationUnit != null ? durationUnit.name().toLowerCase() : null;
    }

    public void setDurationUnit(String str) {
        this.durationUnit = DurationUnit.fromString(str);
    }

    public enum DurationUnit {
        DAY, WEEK, MONTH, YEAR, UNKNOWN;

        public static DurationUnit fromString(String raw) {
            if (raw == null) return null;
            String norm = raw.trim().toLowerCase();

            return switch (norm) {
                case "day", "days" -> DAY;
                case "week", "weeks" -> WEEK;
                case "month", "months" -> MONTH;
                case "year", "years" -> YEAR;
                default -> UNKNOWN;
            };
        }
    }
}
