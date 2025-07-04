package com.example.examine.service.llm;

import com.example.examine.service.EntityService.JournalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.logging.ErrorManager;

@Service
public class LLMService {
    private static final Logger log = LoggerFactory.getLogger(LLMService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public static String objectToJsonString(String str) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(str);  // "escaped string" 형태로 반환됨
    }

    public String callLLM(String prompt) {
        try {
            String jsonPrompt = objectToJsonString(prompt);

            String requestBody = """
                {
                  "model": "llama3",
                  "prompt": %s,
                  "stream": false
                }
                """.formatted(jsonPrompt);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:11434/api/generate",
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RuntimeException("LLM 호출 실패: " + response.getStatusCode());
            }

            LLMResponse wrapper = mapper.readValue(response.getBody(), LLMResponse.class);

            String result = wrapper.response;
            if (!result.trim().endsWith("}")) {
                result += "}";
            }
            log.info("LLM 응답 : {}", result);
            return result;

        } catch (Exception e) {
            log.error("LLM 호출 중 오류 발생", e);
            return null;
        }
    }

}
