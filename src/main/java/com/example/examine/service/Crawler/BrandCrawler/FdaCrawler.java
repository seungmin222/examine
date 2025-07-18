package com.example.examine.service.Crawler.BrandCrawler;

import com.example.examine.dto.response.Crawler.FdaResponse;
import com.example.examine.service.EntityService.JournalService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FdaCrawler {

    private static final Logger log = LoggerFactory.getLogger(FdaCrawler.class);

    public static String fetchFeiByBrandName(String brandName) {
        try {
            String searchUrl = "https://datadashboard.fda.gov/oii/devicefirmsearchresult.htm?search=" + brandName;
            Document searchDoc = Jsoup.connect(searchUrl).userAgent("Mozilla").timeout(5000).get();

            Element link = searchDoc.selectFirst("a[href*='/oii/firmprofile.htm?FEIs=']");
            if (link == null) return null;

            return link.attr("href").replaceAll(".*FEIs=", "").replaceAll("&.*", "");

        } catch (Exception e) {
            log.error("FDA FEI 추출 실패: {}", e.getMessage());
            return null;
        }
    }

    public static FdaResponse fetchStatsByFei(String fei) {
        try {
            String detailUrl = "https://datadashboard.fda.gov/oii/firmprofile.htm?FEIs=" + fei;
            Document detailDoc = Jsoup.connect(detailUrl).userAgent("Mozilla").timeout(5000).get();

            int nai = parseInt(detailDoc.selectFirst("#KPINAI"));
            int vai = parseInt(detailDoc.selectFirst("#KPIVAI"));
            int oai = parseInt(detailDoc.selectFirst("#KPIOAI"));

            log.info("nai:{}, vai:{}, oai:{}", nai, vai, oai);

            return new FdaResponse(nai, vai, oai);

        } catch (Exception e) {
            log.error("FDA NAI/VAI/OAI 추출 실패: {}" , e.getMessage());
            return null;
        }
    }


    private static int parseInt(Element el) {
        if (el == null) return 0;
        try {
            return Integer.parseInt(el.text().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
