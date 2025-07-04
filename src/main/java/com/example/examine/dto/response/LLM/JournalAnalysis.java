package com.example.examine.dto.response.LLM;

import com.example.examine.dto.response.JSEResponse;

import java.util.List;

public record JournalAnalysis (
        Integer participants,
        Integer durationValue,
        String durationUnit,
        Integer blind,
        Boolean parallel,
        String design,
        List<LLMJSE> effects
){
}
