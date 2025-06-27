package com.example.examine.entity;

import com.example.examine.entity.JournalSupplementEffect.JournalSupplementEffect;
import com.example.examine.entity.JournalSupplementEffect.JournalSupplementSideEffect;
import com.example.examine.service.EntityService.JournalService;
import com.example.examine.service.util.CalculateScore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public void setDurationDays() {
        this.durationDays = JournalService.toDays(this.durationValue, this.durationUnit);
    }

    public void setScore() {
        this.score = CalculateScore.calculateJournalScore(participants,durationDays,trialDesign.getName(),blind);
    }
}
