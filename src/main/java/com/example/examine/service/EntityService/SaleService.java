package com.example.examine.service.EntityService;

import com.example.examine.dto.response.Sale.IherbSaleResponse;
import com.example.examine.entity.Sale.IherbCoupon;
import com.example.examine.repository.SaleRepository.IherbCouponRepository;
import com.example.examine.service.Crawler.ShopCrawler.IherbCrawler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SaleService {
    private static final Logger log = LoggerFactory.getLogger(SaleService.class);

    private final IherbCouponRepository iherbCouponRepository;
    private final IherbCrawler iherbCrawler;
    private final AlarmService alarmService;

    @Scheduled(cron = "0 0 9,12,15,21 * * *", zone = "Asia/Seoul")
    public ResponseEntity<String> crawlIherbCoupons() {
        List<IherbCoupon> result = iherbCrawler.crawlCoupons();
        log.info("Crawl Iherb Coupons Result: {}", result);
        return refreshCoupons(result);
    }

    @Transactional
    public ResponseEntity<String> refreshCoupons(List<IherbCoupon> newCoupons) {
        // 기존 쿠폰 가져오기
        List<IherbCoupon> oldCoupons = iherbCouponRepository.findAll();

        // 새 쿠폰만 필터링
        Set<String> oldCodes = oldCoupons.stream()
                .map(IherbCoupon::getDetail) // 또는 getTitle 등 유니크 기준
                .collect(Collectors.toSet());

        List<IherbCoupon> newOnly = newCoupons.stream()
                .filter(c -> !oldCodes.contains(c.getDetail()))
                .toList();

        // DB 갱신
        iherbCouponRepository.deleteAll();
        iherbCouponRepository.saveAll(newCoupons);

        // 알림 생성 (신규 쿠폰 있을 때만)
        if (!newOnly.isEmpty()) {
            alarmService.createNoticeAlarm("아이허브 쿠폰 " + newOnly.size() + "건이 새로 등록되었습니다.");
        }

        return ResponseEntity.ok(newCoupons.size() + "건 저장 완료");
    }


    @Transactional(readOnly = true)
    public List<IherbSaleResponse> getAllCoupons() {
        return iherbCouponRepository.findAll().stream()
                .map(IherbSaleResponse::fromEntity)
                .toList();
    }

}
