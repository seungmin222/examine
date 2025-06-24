package com.example.examine.dto.response;

public record JournalAnalysis (
        Integer participants,
        Integer durationValue,
        String durationUnit,
        Integer blind,
        Boolean parallel,
        String design
        // 추후 성분-효과-효과 크기 쌍도 추가
){
}
