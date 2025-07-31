package com.example.examine.service.EntityService;

import com.example.examine.dto.request.BrandRequest;
import com.example.examine.dto.response.BrandResponse;
import com.example.examine.dto.response.Crawler.FdaResponse;
import com.example.examine.entity.Tag.Brand;
import com.example.examine.repository.TagRepository.BrandRepository;
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
    private final BrandRepository brandRepo;

    public Brand create(BrandRequest request) {
        String fei = request.fei();
        Integer nai = request.nai();
        Integer vai = request.vai();
        Integer oai = request.oai();

        if (fei == null) {
           fei = FdaCrawler.fetchFeiByBrandName(request.engName());
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
                .korName(request.korName())
                .engName(request.engName())
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

        return brandRepo.save(brand);
    }

    public Brand update(Long id, BrandRequest request) {
        Brand brand = brandRepo.findById(id).orElseThrow();

        brand.setKorName(request.korName());
        brand.setEngName(request.engName());
        brand.setCountry(request.country());
        brand.setFei(request.fei());
        brand.setNai(request.nai() != null ? request.nai() : 0);
        brand.setVai(request.vai() != null ? request.vai() : 0);
        brand.setOai(request.oai() != null ? request.oai() : 0);

        double score = CalculateScore.calculateBrandScore(brand);
        brand.setScore(score);
        brand.setTier(CalculateScore.calculateBrandTier(score));

        brandRepo.save(brand);
        return brand;
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAllSorted(Sort sort) {
        return brandRepo.findAll(sort)
                .stream()
                .map(BrandResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> searchByKeyword(String keyword, Sort sort) {
        return brandRepo.findByKeyword(keyword, sort)
                .stream()
                .map(BrandResponse::fromEntity)
                .toList();
    }

    public void delete(Long id) {
        brandRepo.deleteById(id);
    }
}
