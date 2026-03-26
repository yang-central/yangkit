package org.yangcentral.yangkit.data.codec.proto;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache for protobuf descriptors and messages.
 * Provides LRU caching with configurable size and expiration.
 */
public class ProtoCache {
    
    private static volatile ProtoCache instance;
    
    private final ConcurrentHashMap<String, CacheEntry<?>> descriptorCache;
    private final long maxSize;
    private final long expireTimeMs;
    private final AtomicLong accessCount;
    
    private ProtoCache() {
        this.descriptorCache = new ConcurrentHashMap<>();
        this.accessCount = new AtomicLong(0);
        this.maxSize = 1000;
        this.expireTimeMs = TimeUnit.MINUTES.toMillis(30); // Default: 1000 entries, 30 minutes expiry
    }
    
    private ProtoCache(long maxSize, long expireTimeMs) {
        this.maxSize = maxSize;
        this.expireTimeMs = expireTimeMs;
        this.descriptorCache = new ConcurrentHashMap<>();
        this.accessCount = new AtomicLong(0);
    }
    
    /**
     * Get singleton instance of ProtoCache.
     * 
     * @return the singleton instance
     */
    public static ProtoCache getInstance() {
        if (instance == null) {
            synchronized (ProtoCache.class) {
                if (instance == null) {
                    instance = new ProtoCache();
                }
            }
        }
        return instance;
    }
    
    /**
     * Get a cached value.
     * 
     * @param key the cache key
     * @return cached value or null if not found/expired
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CacheEntry<T> entry = (CacheEntry<T>) descriptorCache.get(key);
        
        if (entry == null) {
            return null;
        }
        
        // Check if expired
        if (isExpired(entry)) {
            descriptorCache.remove(key);
            return null;
        }
        
        // Update access time
        entry.lastAccessTime = System.currentTimeMillis();
        accessCount.incrementAndGet();
        
        return entry.value;
    }
    
    /**
     * Put a value in the cache.
     * 
     * @param key the cache key
     * @param value the value to cache
     */
    public <T> void put(String key, T value) {
        // Check if we need to evict
        if (descriptorCache.size() >= maxSize) {
            evictIfNeeded();
        }
        
        CacheEntry<T> entry = new CacheEntry<>(value);
        descriptorCache.put(key, entry);
    }
    
    /**
     * Remove a value from the cache.
     * 
     * @param key the cache key
     */
    public void remove(String key) {
        descriptorCache.remove(key);
    }
    
    /**
     * Clear all cached values.
     */
    public void clear() {
        descriptorCache.clear();
    }
    
    /**
     * Get current cache size.
     * 
     * @return number of entries in cache
     */
    public int size() {
        return descriptorCache.size();
    }
    
    /**
     * Get cache statistics.
     * 
     * @return cache statistics string
     */
    public String getStats() {
        return String.format("Cache[size=%d, maxSize=%d, accesses=%d]", 
                           descriptorCache.size(), maxSize, accessCount.get());
    }
    
    /**
     * Check if a cache entry is expired.
     */
    private boolean isExpired(CacheEntry<?> entry) {
        long now = System.currentTimeMillis();
        return (now - entry.lastAccessTime) > expireTimeMs ||
               (now - entry.creationTime) > expireTimeMs;
    }
    
    /**
     * Evict entries from cache using LRU strategy.
     */
    private void evictIfNeeded() {
        // Find the oldest entry
        String oldestKey = null;
        long oldestTime = Long.MAX_VALUE;
        
        for (String key : descriptorCache.keySet()) {
            CacheEntry<?> entry = descriptorCache.get(key);
            if (entry.lastAccessTime < oldestTime) {
                oldestTime = entry.lastAccessTime;
                oldestKey = key;
            }
        }
        
        if (oldestKey != null) {
            descriptorCache.remove(oldestKey);
        }
    }
    
    /**
     * Cache entry wrapper.
     */
    private static class CacheEntry<T> {
        final T value;
        long creationTime;
        long lastAccessTime;
        
        CacheEntry(T value) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
            this.lastAccessTime = this.creationTime;
        }
    }
}
