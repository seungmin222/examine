// SupplementRequest.java
package com.example.examine.dto;

import java.math.BigDecimal;
import java.util.List;

public record SupplementRequest(   // 프론트엔드에서 받은 문자열 변환
        String korName,
        String engName,
        String dosage,
        BigDecimal cost,
        List<Long> typeIds,
        List<EffectGradeRequest> effectGrades,
        List<SideEffectGradeRequest> sideEffectGrades
) {}
