package com.example.examine.entity.SupplementEffect;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SupplementEffectId implements Serializable, SEId {

    private Long supplementId;
    private Long effectTagId;

    // 기본 생성자 (필수)
    public SupplementEffectId() {}

    // 필드 초기화 생성자 (선택)
    public SupplementEffectId(Long supplementId, Long effectTagId) {
        this.supplementId = supplementId;
        this.effectTagId = effectTagId;
    }

    // Getter & Setter
    @Override
    public Long getSupplementId() {
        return supplementId;
    }

    @Override
    public void setSupplementId(Long supplementId) {
        this.supplementId = supplementId;
    }

    @Override
    public Long getEffectId() {
        return effectTagId;
    }

    @Override
    public void setEffectId(Long effectTagId) {
        this.effectTagId = effectTagId;
    }

    // equals(): 두 객체가 같은 값인지 비교
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplementEffectId)) return false;
        SupplementEffectId that = (SupplementEffectId) o;
        return Objects.equals(supplementId, that.supplementId)
                && Objects.equals(effectTagId, that.effectTagId);
    }

    // hashCode(): 같은 값일 경우 같은 해시코드 반환
    @Override
    public int hashCode() {
        return Objects.hash(supplementId, effectTagId);
    }
}
