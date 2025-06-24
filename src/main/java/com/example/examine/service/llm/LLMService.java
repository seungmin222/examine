package com.example.examine.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LLMService {

    public static String objectToJsonString(String str) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(str);  // "escaped string" 형태로 반환됨
    }
}
