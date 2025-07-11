package com.example.examine.dto.response.LLM;

import java.util.List;

public record JournalAnalysisWithEffects(
        Integer participants,
        Integer durationValue,
        String durationUnit,
        Integer blind,
        Boolean parallel,
        String design,
        List<LLMJSE> effects
) {}
