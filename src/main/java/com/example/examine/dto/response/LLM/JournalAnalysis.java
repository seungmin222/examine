package com.example.examine.dto.response.LLM;

import com.example.examine.dto.response.JSEResponse;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record JournalAnalysis (
        Integer participants,
        Integer durationValue,
        String durationUnit,
        Integer blind,
        Boolean parallel,
        String design,
        @JsonProperty("effects")
        List<Map<String, Object>> effectsRaw

){
}
