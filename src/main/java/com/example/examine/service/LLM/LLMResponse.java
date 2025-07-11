package com.example.examine.service.LLM;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LLMResponse {
    public String model;
    public String created_at;
    public String response;  // 핵심만 여기로!
}
