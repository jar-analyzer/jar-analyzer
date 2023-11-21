package me.n1ar4.jar.analyzer.engine;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache {
    private final int capacity;
    private final LinkedHashMap<String, String> cache;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new LinkedHashMap<String, String>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > LRUCache.this.capacity;
            }
        };
    }

    public synchronized String get(String key) {
        return cache.getOrDefault(key, null);
    }

    public synchronized void put(String key, String value) {
        cache.put(key, value);
    }
}
