package com.example.examine.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SupplementSideEffectId implements Serializable {

    private Long supplementId;
    private Long sideEffectTagId;

    // 기본 생성자 (필수)
    public SupplementSideEffectId() {}

    // 필드 초기화 생성자 (선택)
    public SupplementSideEffectId(Long supplementId, Long effectTagId) {
        this.supplementId = supplementId;
        this.sideEffectTagId = sideEffectTagId;
    }

    // Getter & Setter
    public Long getSupplementId() {
        return supplementId;
    }

    public void setSupplementId(Long supplementId) {
        this.supplementId = supplementId;
    }

    public Long getSideEffectTagId() {
        return sideEffectTagId;
    }

    public void setSideEffectTagId(Long SideEffectTagId) {
        this.sideEffectTagId = sideEffectTagId;
    }

    // equals(): 두 객체가 같은 값인지 비교
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupplementSideEffectId)) return false;
        SupplementSideEffectId that = (SupplementSideEffectId) o;
        return Objects.equals(supplementId, that.supplementId)
                && Objects.equals(sideEffectTagId, that.sideEffectTagId);
    }

    // hashCode(): 같은 값일 경우 같은 해시코드 반환
    @Override
    public int hashCode() {
        return Objects.hash(supplementId, sideEffectTagId);
    }
}
