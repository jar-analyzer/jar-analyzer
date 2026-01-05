/*
 * GPLv3 License
 *
 * Copyright (c) 2022-2026 4ra1n (Jar Analyzer Team)
 *
 * This project is distributed under the GPLv3 license.
 *
 * https://github.com/jar-analyzer/jar-analyzer/blob/master/LICENSE
 */

package me.n1ar4.jar.analyzer.lucene;

import me.n1ar4.jar.analyzer.entity.LuceneSearchResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LuceneSearchCache {
    private static final int CACHE_SIZE = 100;
    private final Map<String, List<LuceneSearchResult>> cache;

    public LuceneSearchCache() {
        this.cache = new LinkedHashMap<String, List<LuceneSearchResult>>(CACHE_SIZE, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, List<LuceneSearchResult>> eldest) {
                return size() > CACHE_SIZE;
            }
        };
    }

    public synchronized void put(String key, List<LuceneSearchResult> results) {
        if (key != null && !key.isEmpty()) {
            cache.put(key, results);
        }
    }

    public synchronized List<LuceneSearchResult> get(String key) {
        return cache.get(key);
    }

    public synchronized boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public synchronized void clear() {
        cache.clear();
    }
}