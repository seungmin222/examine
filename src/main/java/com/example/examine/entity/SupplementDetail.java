package com.example.examine.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "supplement_detail")
public class SupplementDetail {
    @Id
    private Long supplementId; // 직접 ID 필드로 사용

    @OneToOne
    @MapsId // supplementId를 공유해서 primary key로 사용함
    @JoinColumn(name = "supplement_id")
    private Supplement supplement;

    private String intro;
    private String positive;
    private String negative;
    private String mechanism;
    private String dosage;

    public String getIntro() {
        return intro;
    }
    public void setIntro(String intro) {
        this.intro = intro;
    }
    public String getPositive() {
        return positive;
    }
    public void setPositive(String positive) {
        this.positive = positive;
    }
    public String getNegative() {
        return negative;
    }
    public void setNegative(String negative) {
        this.negative = negative;
    }
    public String getMechanism() {
        return mechanism;
    }
    public void setMechanism(String mechanism) {
        this.mechanism = mechanism;
    }
    public String getDosage() {
        return dosage;
    }
    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
    public Supplement getSupplement() {
        return supplement;
    }
    public void setSupplement(Supplement supplement) {
        this.supplement = supplement;
    }
}
