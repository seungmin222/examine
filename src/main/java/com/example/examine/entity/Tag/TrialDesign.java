package com.example.examine.entity.Tag;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "trial_design")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TrialDesign implements Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String korName;

    @Column(unique = true)
    private String engName;

    @Builder.Default
    @Column(nullable = false)
    private String tier = "D";


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getKorName() {
        return korName;
    }

    @Override
    public void setKorName(String korName) {
        this.korName = korName;
    }

    @Override
    public String getEngName() {
        return engName;
    }

    @Override
    public void setEngName(String engName) {
        this.engName = engName;
    }



    @Override
    public String getTier() {
        return tier;
    }

    @Override
    public void setTier(String tier) {
        this.tier = tier;
    }
}
