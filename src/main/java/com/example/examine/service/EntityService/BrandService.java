package com.example.examine.service.EntityService;

import com.example.examine.dto.request.BrandRequest;
import com.example.examine.dto.response.BrandResponse;
import com.example.examine.dto.response.Crawler.FdaResponse;
import com.example.examine.entity.Brand;
import com.example.examine.repository.BrandRepository;
import com.example.examine.service.Crawler.BrandCrawler.FdaCrawler;
import com.example.examine.service.util.CalculateScore;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private static final Logger log = LoggerFactory.getLogger(BrandService.class);
    private final BrandRepository brandRepository;

    public Brand create(BrandRequest request) {
        String fei = request.fei();
        Integer nai = request.nai();
        Integer vai = request.vai();
        Integer oai = request.oai();

        if (fei == null) {
           fei = FdaCrawler.fetchFeiByBrandName(request.name());
        }
        if (nai==null || vai==null || oai==null){
           FdaResponse fdaResponse = FdaCrawler.fetchStatsByFei(fei);
           if(fdaResponse!=null) {
               nai = fdaResponse.nai();
               vai = fdaResponse.vai();
               oai = fdaResponse.oai();
           }
        }

        Brand brand = Brand.builder()
                .name(request.name())
                .country(request.country())
                .fei(fei)
                .nai(nai != null ? nai : 0)
                .vai(vai != null ? vai : 0)
                .oai(oai != null ? oai : 0)
                .build();

        double score = CalculateScore.calculateBrandScore(brand);
        String tier = CalculateScore.calculateBrandTier(score);

        brand.setScore(score);
        brand.setTier(tier);

        return brandRepository.save(brand);
    }

    public Brand update(Long id, BrandRequest request) {
        Brand brand = brandRepository.findById(id).orElseThrow();

        brand.setName(request.name());
        brand.setCountry(request.country());
        brand.setFei(request.fei());
        brand.setNai(request.nai() != null ? request.nai() : 0);
        brand.setVai(request.vai() != null ? request.vai() : 0);
        brand.setOai(request.oai() != null ? request.oai() : 0);

        double score = CalculateScore.calculateBrandScore(brand);
        brand.setScore(score);
        brand.setTier(CalculateScore.calculateBrandTier(score));

        return brand;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAllSorted(Sort sort) {
        return brandRepository.findAll(sort)
                .stream()
                .map(BrandResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> searchByKeyword(String keyword, Sort sort) {
        return brandRepository.findByNameContainingIgnoreCase(keyword, sort)
                .stream()
                .map(BrandResponse::fromEntity)
                .toList();
    }

    public void delete(Long id) {
        brandRepository.deleteById(id);
    }
}
