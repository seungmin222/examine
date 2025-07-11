package com.example.examine.service.Crawler;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.examine.service.Crawler.DateParser.ClinicalTrialsDateParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class SemanticScholarCrawler implements JournalCrawler {
    @Override
    public JournalCrawlerMeta extract(String webUrl) throws IOException  {
        // DOI 또는 Semantic Scholar ID
        Pattern p = Pattern.compile("semanticscholar\\.org/paper/.+/([a-f0-9]{40})");
        Matcher m = p.matcher(webUrl);
        String apiUrl = "";
        String paperId = "";
        if (m.find()) {
            paperId = m.group(1);
            apiUrl = "https://api.semanticscholar.org/graph/v1/paper/"
                    + paperId + "?fields=title,abstract,citationCount,year";
        }
        System.out.println(apiUrl);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new URL(apiUrl));

        String title = root.path("title").asText();
        String summary = root.path("abstract").asText();
        int citationCount = root.path("citationCount").asInt();
        String date = root.path("publicationDate").asText();
        String year = root.path("year").asText();
        
        if(date==null){
            date = year + "-01-01";
        }

        System.out.println("Title: " + title);
        System.out.println("Abstract: " + summary);
        System.out.println("Citations: " + citationCount);

        return new JournalCrawlerMeta(title, citationCount ,summary, ClinicalTrialsDateParser.parse(date));
    }                                                         //"yyyy-mm-dd" 형식
}
