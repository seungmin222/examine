// PubmedRequest.java
package com.example.examine.dto;

import java.util.List;

public record PubmedRequest(
        String title,
        String link,
        List<Long> supplements,
        List<Long> effects,
        List<Long> sideEffects,
        String summary,
        Long trial_design_id,
        String trial_length,
        Integer participants
) {}
