package com.example.examine.entity.SupplementEffect;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class SupplementSideEffectId implements Serializable, SEId {

    private Long supplementId;
    private Long sideEffectTagId;

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
        return sideEffectTagId;
    }

    @Override
    public void setEffectId(Long SideEffectTagId) {
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
