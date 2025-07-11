package com.example.examine.entity.SupplementType;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Getter
@Setter
public class SupplementTypeId implements Serializable {

    private Long supplementId;
    private Long typeTagId;

    // equals(): 두 객체가 같은 값인지 비교
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof com.example.examine.entity.SupplementType.SupplementTypeId)) return false;
        com.example.examine.entity.SupplementType.SupplementTypeId that = (com.example.examine.entity.SupplementType.SupplementTypeId) o;
        return Objects.equals(supplementId, that.supplementId)
                && Objects.equals(typeTagId, that.typeTagId);
    }

    // hashCode(): 같은 값일 경우 같은 해시코드 반환
    @Override
    public int hashCode() {
        return Objects.hash(supplementId,typeTagId);
    }
}
