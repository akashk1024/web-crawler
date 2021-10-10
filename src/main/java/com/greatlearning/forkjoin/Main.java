package com.greatlearning.forkjoin;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class Main {
    public static void main(String[] args) {
        String startingUrl = "http://example.com/";
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        UrlConnectionReader urlConnectionReader = new UrlConnectionReader(startingUrl);
        Set<String> urlsWithDepth0 = forkJoinPool.invoke(urlConnectionReader);
        System.out.println("Total number of urls extracted at depth 0: " + urlsWithDepth0.size());
        int depth = 2;
        long start, end, duration;
        Set<String> urlsWithNextDepth = urlsWithDepth0;
        for (int i = 1; i < depth; i++) {
            start = Instant.now().toEpochMilli();
            UrlConnectionReader intermediateConnectionReader = new UrlConnectionReader(urlsWithNextDepth);
            urlsWithNextDepth = forkJoinPool.invoke(intermediateConnectionReader);
            end = Instant.now().toEpochMilli();
            duration = end - start;
            System.out.println("Total number of urls extracted at depth " + i + ": " + urlsWithNextDepth.size());
            System.out.println("Time taken at depth " + i + ": " + duration);
        }
    }
}
