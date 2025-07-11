package com.example.examine.service.EntityService;

import com.example.examine.controller.DetailController;
import com.example.examine.dto.request.DetailRequest;
import com.example.examine.dto.request.SupplementRequest;
import com.example.examine.dto.response.*;
import com.example.examine.entity.*;
import com.example.examine.entity.Tag.Supplement;
import com.example.examine.entity.Tag.TypeTag;
import com.example.examine.entity.detail.SupplementDetail;
import com.example.examine.repository.Detailrepository.SupplementDetailRepository;
import com.example.examine.repository.JSERepository.JournalSupplementEffectRepository;
import com.example.examine.repository.JSERepository.JournalSupplementSideEffectRepository;
import com.example.examine.repository.SERepository.SupplementEffectRepository;
import com.example.examine.repository.SERepository.SupplementSideEffectRepository;
import com.example.examine.repository.TagRepository.SupplementRepository;
import com.example.examine.repository.TagRepository.TypeTagRepository;
import com.example.examine.service.LLM.LLMResponse;
import com.example.examine.service.LLM.LLMService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.*;

// 서비스
@Service
@RequiredArgsConstructor
public class SupplementService {
    private static final Logger log = LoggerFactory.getLogger(DetailController.class);

    private final SupplementRepository supplementRepo;
    private final TypeTagRepository typeRepo;
    private final SupplementDetailRepository supplementDetailRepo;
    private final SupplementEffectRepository supplementEffectRepo;
    private final SupplementSideEffectRepository supplementSideEffectRepo;
    private final JournalSupplementEffectRepository jseRepo;
    private final JournalSupplementSideEffectRepository jsseRepo;
    private final JournalService journalService;

    public ResponseEntity<String> create(SupplementRequest dto) {
        if (supplementRepo.findByKorNameAndEngName(dto.korName(), dto.engName()).isPresent()) {
            return ResponseEntity.badRequest().body("이미 같은 이름의 성분이 존재합니다.");
        }
        if(dto.korName().isBlank()&&dto.engName().isBlank()){
            return ResponseEntity.badRequest().body("이름을 입력해 주세요.");
        }

        Supplement supplement = Supplement.builder()
                .korName(dto.korName())
                .engName(dto.engName())
                .dosageValue(dto.dosageValue())
                .dosageUnit(Supplement.DosageUnit.fromString(dto.dosageUnit()))
                .cost(dto.cost())
                .build();

        List<TypeTag> newTypes = dto.typeIds() != null
                ? new ArrayList<>(typeRepo.findAllById(dto.typeIds()))
                : new ArrayList<>();

        supplement.getTypes().addAll(newTypes);

        SupplementAnalysis result = analyze(supplement.getEngName(),supplement.getKorName());
        applyAnalysis(supplement, result);

        supplementRepo.save(supplement);
        SupplementDetail supplementDetail = SupplementDetail.builder()
                .supplement(supplement)
                .overview("")
                .intro("")
                .positive("")
                .negative("")
                .mechanism("")
                .dosage("")
                .build();
        supplementDetailRepo.save(supplementDetail);

        return ResponseEntity.ok("성분 추가 완료");
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


    public ResponseEntity<String> update(Long id, SupplementRequest dto) {

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

        supplementRepo.save(supplement);
        return ResponseEntity.ok("성분 업데이트 완료");

    }

    public List<SupplementResponse> toSupplementResponse(List<Long> ids, Sort sort) {
        if (ids.isEmpty()) return List.of();

        List<Supplement> supplements = supplementRepo.fetchTypesByIds(ids, sort);
        supplementRepo.fetchEffectsByIds(ids);
        supplementRepo.fetchSideEffectsByIds(ids);

        return supplements.stream()
                .map(SupplementResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupplementResponse> findAll(Sort sort) {
        List<Long> ids = supplementRepo.findAllIds();
        return toSupplementResponse(ids, sort);
    }

    @Transactional(readOnly = true)
    public List<SupplementResponse> search(String keyword, Sort sort) {
        List<Long> ids = supplementRepo.findIdsByKeyword(keyword);
        return toSupplementResponse(ids, sort);
    }

    @Transactional(readOnly = true)
    public SupplementDetailResponse findDetail(Long id, Sort sort) {
        Supplement supplement = supplementRepo.findById(id).orElseThrow(() -> new NoSuchElementException("성분이 없습니다"));

        SupplementResponse supplementResponse = SupplementResponse.fromEntity(supplement);
        List <SupplementResponse> supplementResponses = new ArrayList<>();
        supplementResponses.add(supplementResponse);
        DetailResponse detail = DetailResponse.fromEntity(supplementDetailRepo.findById(id).orElseThrow(() -> new NoSuchElementException("상세 정보가 없습니다")));
        List<TagResponse> effects = supplement.getEffects().stream()
                .map(e->new TagResponse(e.getId().getEffectId(), e.getEffectKorName(), e.getEffectEngName(), e.getTier()))
                .toList();
        List<TagResponse> sideEffects = supplement.getSideEffects().stream()
                .map(e->new TagResponse(e.getId().getEffectId(), e.getEffectKorName(), e.getEffectEngName(), e.getTier()))
                .toList();
        return new SupplementDetailResponse(supplementResponses, detail, effects, sideEffects);
    }

    @Transactional(readOnly = true)
    public List<JournalResponse> findJournals(Long id, Sort sort) {
        Set<Long> journalIds = new HashSet<>();
        journalIds.addAll(jseRepo.findJournalsBySupplementId(id));
        journalIds.addAll(jsseRepo.findJournalsBySupplementId(id));

        return journalService.toJournalResponses(journalIds.stream().toList(), sort);
    }

    @Transactional(readOnly = true)
    public List<SupplementResponse> findFiltered(
            List<Long> typeIds,
            List<Long> effectIds,
            List<Long> sideEffectIds,
            List<String> tiers,
            Sort sort
    ){
        List<Long> result = null;

        if (typeIds != null) {
            result = new ArrayList<>(supplementRepo.findIdsByTypes(typeIds));
        }
        if (effectIds != null) {
            List<Long> temp = supplementRepo.findIdsByEffects(effectIds);
            result = (result == null) ? temp : retainIntersection(result, temp);
        }
        if (sideEffectIds != null) {
            List<Long> temp = supplementRepo.findIdsBySideEffects(sideEffectIds);
            result = (result == null) ? temp : retainIntersection(result, temp);
        }
        if (tiers != null) {
            List<Long> temp = supplementRepo.findIdsByTiers(tiers);
            result = (result == null) ? temp : retainIntersection(result, temp);
        }

        result = (result == null) ? new ArrayList<>() : result;

        return toSupplementResponse(result, sort);
    }

    private List<Long> retainIntersection(List<Long> list1, List<Long> list2) {
        return list1.stream()
                .filter(new HashSet<>(list2)::contains)
                .toList();
    }

    public ResponseEntity<String> delete(Long id) {
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


    public ResponseEntity<String> detailUpdate(@RequestBody DetailRequest dto) {
        log.info("🔄 수정 요청 들어옴 - ID: {}", dto.id());
        log.info("📥 받은 데이터: {}", dto);

        Optional<SupplementDetail> opt = supplementDetailRepo.findById(dto.id());
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        SupplementDetail s = opt.get();
        s.setIntro(dto.intro());
        s.setPositive(dto.positive());
        s.setNegative(dto.negative());
        s.setMechanism(dto.mechanism());
        s.setDosage(dto.dosage());

        supplementDetailRepo.save(s);
        return ResponseEntity.ok("성분 업데이트 완료");
    }

}
