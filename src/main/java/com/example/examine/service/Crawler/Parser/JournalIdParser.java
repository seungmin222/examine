package com.example.examine.service.Crawler.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JournalIdParser {
    public static String pubmedJournalId(String url) {
        Pattern p = Pattern.compile("pubmed\\.ncbi\\.nlm\\.nih\\.gov/(\\d+)");
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    public static String clinicalTrialsId(String url) {
        Pattern p = Pattern.compile("clinicaltrials\\.gov/ct2/show/(\\w+)");
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    public static String semanticScholarId(String url) {
        Pattern p = Pattern.compile("semanticscholar\\.org/paper/.*/(\\w+)$");
        Matcher m = p.matcher(url);
        return m.find() ? m.group(1) : null;
    }

}
