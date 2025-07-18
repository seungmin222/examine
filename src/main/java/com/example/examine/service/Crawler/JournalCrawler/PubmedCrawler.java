package com.example.examine.service.Crawler.JournalCrawler;

import com.example.examine.service.Crawler.DateParser.PubmedDateParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PubmedCrawler implements JournalCrawler {
    @Override
    public JournalCrawlerMeta extract(String webUrl) throws IOException {
        Pattern p = Pattern.compile("pubmed\\.ncbi\\.nlm\\.nih\\.gov/(\\d+)");
        Matcher m = p.matcher(webUrl);
        String pId = "";
        String apiUrl = "";
        String efetchUrl = "";
        if (m.find()) {
            pId = m.group(1);
            apiUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=" + pId + "&retmode=json";
            efetchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=" + pId + "&retmode=xml";
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new URL(apiUrl));
        Document xmlDoc = Jsoup.connect(efetchUrl).get();
        JsonNode resultNode = root.path("result").path(pId);
        String title = resultNode.path("title").asText();
        String pubDate = resultNode.path("pubdate").asText("");
        String ePubDate = resultNode.path("epubdate").asText("");

        String date = ePubDate.length() > pubDate.length() ? ePubDate : pubDate;
        Elements abstracts = xmlDoc.select("Abstract > AbstractText");

        StringBuilder sb = new StringBuilder();
        for (Element abs : abstracts) {
            sb.append(abs.text()).append("\n");
        }
        String summary = sb.toString().trim();
        return new JournalCrawlerMeta(title, null ,summary, PubmedDateParser.parse(date));
    }
}