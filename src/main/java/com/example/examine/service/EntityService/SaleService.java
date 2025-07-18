package com.example.examine.service.EntityService;

import com.example.examine.dto.response.Sale.IherbSaleResponse;
import com.example.examine.entity.Sale.IherbCoupon;
import com.example.examine.repository.SaleRepository.IherbCouponRepository;
import com.example.examine.service.Crawler.ShopCrawler.IherbCrawler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final IherbCouponRepository iherbCouponRepository;
    private final IherbCrawler iherbCrawler;

    @Scheduled(cron = "0 0 9,21 * * *", zone = "Asia/Seoul")
    public ResponseEntity<String> crawlIherbCoupons() {
        List<IherbCoupon> result = iherbCrawler.crawlCoupons();
        return refreshCoupons(result);
    }

    public ResponseEntity<String> refreshCoupons(List<IherbCoupon> coupons) {
        iherbCouponRepository.deleteAll();
        iherbCouponRepository.saveAll(coupons);
        return ResponseEntity.ok(coupons.size() + "건 저장 완료");
    }

    @Transactional(readOnly = true)
    public List<IherbSaleResponse> getAllCoupons() {
        return iherbCouponRepository.findAll().stream()
                .map(IherbSaleResponse::fromEntity)
                .toList();
    }

}
