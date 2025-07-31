package com.example.examine.service.Crawler.JournalCrawler;

import com.example.examine.service.Crawler.Parser.ClinicalTrialsDateParser;
import com.example.examine.service.util.EnumService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClinicalTrialsCrawler implements JournalCrawler {
    @Override
    public JournalCrawlerMeta extract(String webUrl) throws IOException {
        Pattern p = Pattern.compile("NCT\\d+");
        Matcher m = p.matcher(webUrl);
        String apiUrl = "https://clinicaltrials.gov/api/v2/studies/";
        String nctId = "";
        if (m.find()) {
            nctId = m.group();
            apiUrl += nctId;
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new URL(apiUrl));
        System.out.println(apiUrl);
        String title = root.at("/protocolSection/identificationModule/briefTitle").asText();
        Integer participants = root.at("/protocolSection/designModule/enrollmentInfo/count").asInt();
        String summary = root.at("/protocolSection/descriptionModule/briefSummary").asText();
        String date = root.at("/protocolSection/statusModule/studyFirstPostDateStruct/date").asText();
        System.out.println(title);
        System.out.println(summary);
        System.out.println(date);
        return new JournalCrawlerMeta(EnumService.JournalSiteType.CLINICAL_TRIALS,
                nctId, title, participants, summary,
                ClinicalTrialsDateParser.parse(date)
        );
    }

}