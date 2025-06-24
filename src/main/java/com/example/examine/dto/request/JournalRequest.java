package com.example.examine.dto.request;

import com.example.examine.entity.*;
import java.util.List;
import java.time.LocalDate;

public record JournalRequest(
        String link,
        List<JSERequest> effects,
        List<JSERequest> sideEffects,
        Long trialDesignId,
        Integer blind,
        Boolean parallel,
        Integer durationValue,
        String durationUnit,
        Integer participants
) {
}