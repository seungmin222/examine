package com.example.examine.service.Crawler.ShopCrawler;

import com.example.examine.dto.response.Crawler.ProductCrawlerResponse;
import com.example.examine.entity.Sale.IherbCoupon;
import com.example.examine.service.Crawler.Parser.IherbDateParser;
import com.example.examine.service.util.EnumService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class IherbCrawler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<IherbCoupon> crawlCoupons() {
        List<IherbCoupon> result = new ArrayList<>();
        String TARGET_URL = "https://kr.iherb.com/info/sales-and-offers";

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

    public static String iHerbProductId(String url) {
        if (url == null) return null;
        try {
            Pattern pattern = Pattern.compile("/pr/(?:.*?/)?(\\d+)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            log.warn("🔎 아이허브 링크 파싱 실패: {}", e.getMessage());
        }
        return null;
    }


    public static ProductCrawlerResponse productCrawl(String url) {
        try {
            String productId = iHerbProductId(url);
            String apiUrl = "https://catalog.app.iherb.com/product/" + productId;
            HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() != 200)
                throw new RuntimeException("HTTP 응답 오류: " + conn.getResponseCode());

            String responseBody = new Scanner(conn.getInputStream()).useDelimiter("\\A").next();
            JsonNode root = objectMapper.readTree(responseBody);

            String name = root.path("displayName").asText(null);

            // 💡 가격은 string으로 받아서 BigDecimal로 파싱
            String priceStr = root.path("listPriceAmount").asText();
            BigDecimal price = (priceStr == null || priceStr.isBlank()) ? null : new BigDecimal(priceStr);

            // 💡 단위당 가격
            String pricePerUnitStr = root.path("pricePerUnit").asText();
            BigDecimal pricePerDose = extractNumericPrice(pricePerUnitStr);

            String brandName = root.path("brandName").asText();

            log.info("아이허브 크롤링 성공: {}, {}, {}, {}", name, price, pricePerDose, brandName);
            return new ProductCrawlerResponse(EnumService.ProductSiteType.IHERB,
                    productId, name, price, pricePerDose, brandName
            );

        } catch (Exception e) {
            e.printStackTrace();
            log.error("아이허브 크롤링 실패: {}", e.getMessage());
            return null;
        }
    }


    private static BigDecimal extractNumericPrice(String formattedPrice) {
        // 예시: "₩570/제공량" → "570"
        try {
            String numeric = formattedPrice.replaceAll("[^\\d.]", ""); // 숫자와 '.'만 남김
            return new BigDecimal(numeric);
        } catch (Exception e) {
            return null;
        }
    }
}
