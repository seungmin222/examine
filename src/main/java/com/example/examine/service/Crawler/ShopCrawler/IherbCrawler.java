package com.example.examine.service.Crawler.ShopCrawler;

import com.example.examine.dto.response.Crawler.IherbProductResponse;
import com.example.examine.entity.Sale.IherbCoupon;
import com.example.examine.service.Crawler.DateParser.IherbDateParser;
import com.example.examine.service.EntityService.SaleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class IherbCrawler {

    private static final String TARGET_URL = "https://kr.iherb.com/info/sales-and-offers";

    public List<IherbCoupon> crawlCoupons() {
        List<IherbCoupon> result = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(TARGET_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(5000)
                    .get();

            Elements cards = doc.select("li.promotion-card");

            for (Element card : cards) {
                Element img = card.selectFirst("img.cms-banner-img");
                String alt = img != null ? img.attr("alt") : "";

                Element input = card.selectFirst("input.code");
                String code = input != null ? input.attr("value") : null;

                Element link = card.selectFirst("a.promo-link");
                String href = link != null ? link.attr("href") : "";

                Element expires = card.selectFirst(".promo-expires-at");
                LocalDateTime expiresAt = expires != null
                        ? IherbDateParser.parseKoreanTime(expires.text())
                        : null;

                IherbCoupon coupon = IherbCoupon.builder()
                        .detail(alt)
                        .promoCode(code)
                        .link(href)
                        .expiresAt(expiresAt)
                        .build();

                result.add(coupon);
            }

            log.info("✅ 아이허브 쿠폰 {}건 크롤링 완료", result.size());

        } catch (Exception e) {
            log.error("📛 iHerb 크롤링 실패", e);
        }
// 크롤링 결과 다 만들고 나서 정렬
        result.sort((a, b) -> {
            boolean aHasCode = a.getPromoCode() != null;
            boolean bHasCode = b.getPromoCode() != null;

            // 1. 코드 유무 비교 (있는 게 먼저)
            if (aHasCode && !bHasCode) return -1;
            if (!aHasCode && bHasCode) return 1;

            // 2. 둘 다 코드 있음 → 할인율 비교 (내림차순)
            if (aHasCode && bHasCode) {
                int aDiscount = extractDiscountRate(a.getDetail());
                int bDiscount = extractDiscountRate(b.getDetail());
                return Integer.compare(bDiscount, aDiscount);
            }

            // 3. 둘 다 코드 없음 → 순서 그대로
            return 0;
        });

        return result;
    }

    private int extractDiscountRate(String altText) {
        if (altText == null) return 0;
        Matcher matcher = Pattern.compile("(\\d+)%").matcher(altText);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    public static IherbProductResponse productCrawl(String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .get();

            String name = extractText(doc, "#name");
            String imageUrl = extractAttr(doc, "#iherb-product-image", "src");
            String price = extractText(doc, ".list-price");
            String pricePerDose = extractText(doc, ".list-price-per-unit");

            return new IherbProductResponse(name, imageUrl, price, pricePerDose);

        } catch (Exception e) {
            log.warn("❌ 아이허브 크롤링 실패: {}", e.getMessage());
            return null;
        }
    }

    private static String extractText(Document doc, String selector) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.text() : null;
    }

    private static String extractAttr(Document doc, String selector, String attr) {
        Element el = doc.selectFirst(selector);
        return el != null ? el.attr(attr) : null;
    }
}
