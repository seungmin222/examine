package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.request.DetailRequest;
import com.example.examine.dto.request.SupplementRequest;
import com.example.examine.dto.response.*;
import com.example.examine.entity.*;
import com.example.examine.entity.SupplementEffect.SE;
import com.example.examine.entity.SupplementEffect.SupplementEffect;
import com.example.examine.entity.SupplementEffect.SupplementSideEffect;
import com.example.examine.repository.*;
import com.example.examine.service.llm.LLMResponse;
import com.example.examine.service.llm.LLMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.*;

// 서비스
@Service
public class SupplementService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final SupplementDetailRepository supplementDetailRepo;
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final JournalSupplementEffectRepository journalSupplementEffectRepo;
    private final JournalSupplementSideEffectRepository journalSupplementSideEffectRepo;

    public SupplementService(SupplementRepository supplementRepo,
                             TypeTagRepository typeRepo,
                             SupplementDetailRepository supplementDetailRepo,
                             SupplementEffectRepository supplementEffectRepo,
                             SupplementSideEffectRepository supplementSideEffectRepo,
                             JournalSupplementEffectRepository journalSupplementEffectRepo,
                             JournalSupplementSideEffectRepository journalSupplementSideEffectRepo) {
        this.supplementRepo = supplementRepo;
        this.typeRepo = typeRepo;
        this.supplementDetailRepo = supplementDetailRepo;
        this.supplementEffectRepo = supplementEffectRepo;
        this.supplementSideEffectRepo = supplementSideEffectRepo;
        this.journalSupplementEffectRepo = journalSupplementEffectRepo;
        this.journalSupplementSideEffectRepo = journalSupplementSideEffectRepo;
    }

    public ResponseEntity<?> create(SupplementRequest dto) {
        if (supplementRepo.findByKorNameAndEngName(dto.korName(), dto.engName()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 이름의 성분이 존재합니다.");
        }
        if(dto.korName().isBlank()&&dto.engName().isBlank()){
            return ResponseEntity.badRequest().body("이름을 입력해 주세요.");
        }

        Supplement supplement = new Supplement();

        supplement.setKorName(dto.korName());
        supplement.setEngName(dto.engName());
        supplement.setDosageValue(dto.dosageValue());
        supplement.setDosageUnit(dto.dosageUnit());
        supplement.setCost(dto.cost());

        List<TypeTag> newTypes = dto.typeIds() != null
                ? new ArrayList<>(typeRepo.findAllById(dto.typeIds()))
                : new ArrayList<>();

        supplement.getTypes().addAll(newTypes);

        SupplementAnalysis result = analyze(supplement.getEngName(),supplement.getKorName());
        applyAnalysis(supplement, result);

        return ResponseEntity.ok(supplementRepo.save(supplement));
    }

    public SupplementAnalysis analyze(String engName, String korName) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String prompt;

        if (!engName.isBlank()) {
            // 영어 이름 있으면: 한국어 이름 + 복용량 추출
            prompt = """
                    You will be given the English name of a supplement. Your task is to extract the following fields in compact JSON format.
                    No explanation, no preamble, no code block.
                    Return only valid compact JSON. :
                    {
                      "korName": string or null,
                      "dosageValue": number or null,
                      "dosageUnit": "ug" | "mg" | "g" | "iu" | null
                    }
                    
                    English name: %s
                    """.formatted(engName);

        } else {
            // 한국어 이름만 있을 때: 영어 이름 + 복용량 추출
            prompt = """
                    You will be given the Korean name of a supplement. Your task is to extract the following fields in compact JSON format.
                    No explanation, no preamble, no code block.
                    Return only valid compact JSON. :
                    
                    {
                      "engName": string or null,
                      "dosageValue": number or null,
                      "dosageUnit": "ug" | "mg" | "g" | "iu" | null
                    }
                    
                    Korean name: %s
                    """.formatted(korName);

        }

        try {
            String jsonPrompt = LLMService.objectToJsonString(prompt);

            String requestBody = """
                    {
                      "model": "llama3",
                      "prompt": %s,
                      "stream": false
                    }
                    """.formatted(jsonPrompt);

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "http://localhost:11434/api/generate", entity, String.class);

            ObjectMapper mapper = new ObjectMapper();

            LLMResponse wrapper = mapper.readValue(response.getBody(), LLMResponse.class);

            String raw = wrapper.response;
            if (!raw.trim().endsWith("}")) {
                raw += "}";
            }

            System.out.println("LLM 응답: " + raw);

            return mapper.readValue(raw, SupplementAnalysis.class);

        } catch (Exception e) {
            log.error("LLM 분석 실패", e); // 혹은 logger.error("LLM 분석 실패", e);
        }
        return new SupplementAnalysis(null, null, null, null);
    }

    public void applyAnalysis(Supplement supplement, SupplementAnalysis result) {
        if (supplement.getEngName().isBlank()) {
            supplement.setEngName(result.engName());
        }
        if (supplement.getKorName().isBlank()) {
            supplement.setKorName(result.korName());
        }
        if (supplement.getDosageValue() == null) {
            supplement.setDosageValue(result.dosageValue());
        }
        if (supplement.getDosageUnit() == null || supplement.getDosageUnit().isBlank()) {
            supplement.setDosageUnit(result.dosageUnit());
        }

    }


    public ResponseEntity<?> update(Long id, SupplementRequest dto) {

        Optional<Supplement> opt = supplementRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Supplement supplement = opt.get();
        supplement.setKorName(dto.korName());
        supplement.setEngName(dto.engName());
        supplement.setDosageValue(dto.dosageValue());
        supplement.setDosageUnit(dto.dosageUnit());
        supplement.setCost(dto.cost());

        List<TypeTag> newTypes = dto.typeIds()  != null
                ? new ArrayList<>(typeRepo.findAllById(dto.typeIds()))
                : new ArrayList<>();

        supplement.getTypes().clear(); // 기존 컬렉션 유지
        supplement.getTypes().addAll(newTypes); // 내부만 갱신

        Supplement updated = supplementRepo.save(supplement);
        return ResponseEntity.ok(updated);
    }

    public List<SupplementResponse> findAll(Sort sort){
        return supplementRepo.findAll(sort)
                .stream()
                .map(SupplementResponse::fromEntity)
                .toList();
    }

    public List<SupplementResponse> findOne(Long id) {
        return supplementRepo.findById(id)
                .stream()
                .map(SupplementResponse::fromEntity)
                .toList();
    }


    public List<SupplementResponse> search(String keyword, Sort sort){
        return supplementRepo.findByKorNameContainingIgnoreCaseOrEngNameContainingIgnoreCase(
                keyword, keyword, sort)
                .stream()
                .map(SupplementResponse::fromEntity)
                .toList();
    }

    public List<SupplementResponse> findFiltered(
            List<Long> typeIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            List<String> tiers,
            Sort sort
    ){
        return supplementRepo.findFiltered(typeIds, effectIds, sideEffectIds, tiers, sort)
                .stream()
                .map(SupplementResponse::fromEntity)
                .toList();
    }

    public List<JournalResponse> journals(Long id) {
        Set<Journal> journalSet = new HashSet<>();
        journalSupplementEffectRepo.findAllBySupplementId(id)
                .forEach(e -> journalSet.add(e.getJournal()));
        journalSupplementSideEffectRepo.findAllBySupplementId(id)
                .forEach(e -> journalSet.add(e.getJournal()));

        return journalSet.stream()
                .map(JournalResponse::fromEntity)
                .toList();
    }

    public ResponseEntity<?> delete(Long id) {
        if (!supplementRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        supplementRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public DetailResponse detail(@PathVariable Long id) {
        Supplement supplement = supplementRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 ID의 성분이 없습니다"));

        SupplementDetail detail = supplementDetailRepo.findBySupplement(supplement)
                .orElseThrow(() -> new NoSuchElementException("상세 정보가 없습니다"));

        return DetailResponse.fromEntity(detail);
    }


    public ResponseEntity<?> detailUpdate(@PathVariable Long id, @RequestBody DetailRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", id);
        log.info("📥 받은 데이터: {}", dto);

        Optional<SupplementDetail> opt = supplementDetailRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        SupplementDetail s = opt.get();
        s.setIntro(dto.intro());
        s.setPositive(dto.positive());
        s.setNegative(dto.negative());
        s.setMechanism(dto.mechanism());
        s.setDosage(dto.dosage());

        SupplementDetail updated = supplementDetailRepo.save(s);
        return ResponseEntity.ok(updated);
    }

}
