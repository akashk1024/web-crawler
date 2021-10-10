package com.greatlearning.forkjoin;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class UrlConnectionReader extends RecursiveTask<Set<String>> {
    public static final int THRESHOLD = 10;
    private static final List<String> urlsToVisit = new ArrayList<>();
    private static final Map<String, Boolean> visited = new HashMap<>();
    private final int start;
    private final int end;
    private final Path path = Paths.get("web-crawler.txt");

    public UrlConnectionReader(int beginIndex, int endIndex) {
        start = beginIndex;
        end = endIndex;
    }

    public UrlConnectionReader(String startingUrl) {
        urlsToVisit.add(startingUrl);
        start = 0;
        end = 1;
    }

    public UrlConnectionReader(Set<String> urlsWithDepth1) {
        start = urlsToVisit.size();
        urlsToVisit.addAll(urlsWithDepth1);
        end = urlsToVisit.size();
    }

    @Override
    protected Set<String> compute() {
        if (end - start < THRESHOLD) {
            Set<String> childUrls = new HashSet<>();
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = Files.newBufferedWriter(path);
                StringBuilder sb = new StringBuilder();
                for (int i = start; i < end; i++) {
                    Document doc = Jsoup.connect(urlsToVisit.get(i)).get();
                    Elements links = doc.body().select("a[href]");
                    for (Element link : links) {
                        String url = link.attr("abs:href");
                        if (!visited.getOrDefault(url, false)) {
                            //System.out.println("Child Link: "+ url + " Title: "+link.text());
                            sb.append("Child Link: ").append(url).append(" Title: ").append(link.text()).append("\n");
                            childUrls.add(url);
                        }
                    }
                    visited.put(urlsToVisit.get(i), true);
                    assert bufferedWriter != null;
                    bufferedWriter.write(sb.toString());
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return childUrls;
        } else {
            int mid = (start + end) / 2;
            UrlConnectionReader firstHalf = new UrlConnectionReader(start, mid);
            firstHalf.fork();
            UrlConnectionReader secondHalf = new UrlConnectionReader(mid, end);
            Set<String> completeUrlList = secondHalf.compute();
            completeUrlList.addAll(firstHalf.join());
            return completeUrlList;
        }
    }
}
