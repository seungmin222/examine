package com.example.examine.entity.SupplementEffect;

import com.example.examine.entity.Effect.Effect;
import com.example.examine.entity.Supplement;

import java.math.BigDecimal;

public interface SE {
    SEId getId();
    void setId(SEId id);

    Effect getEffect(); // 공통 인터페이스
    void setEffect(Effect effect);

    Supplement getSupplement();
    void setSupplement(Supplement supplement);

    BigDecimal getTotalScore();
    void setTotalScore(BigDecimal totalScore);

    Integer getTotalParticipants();
    void setTotalParticipants(Integer totalParticipants);

    BigDecimal getFinalScore();
    void setFinalScore();

    String getTier();
    void setTier(String tier);
}
