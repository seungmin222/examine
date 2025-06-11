package com.example.examine.dto;

import com.example.examine.entity.*;

public record DurationRequest(
    Integer value,
    String unit,
    Integer days
){}
