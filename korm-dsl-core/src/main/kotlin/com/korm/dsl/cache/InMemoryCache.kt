package com.korm.dsl.cache

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory LRU (Least Recently Used) cache implementation
 * Thread-safe and supports TTL expiration
 */
class InMemoryCache(
    private val config: CacheConfig = CacheConfig()
) : Cache {

    // Storage for cache entries
    private val cache = ConcurrentHashMap<String, CacheEntry<Any>>()

    // LRU tracking - LinkedHashMap with access order
    private val accessOrder = object : LinkedHashMap<String, Long>(
        config.maxSize,
        0.75f,
        true // access order
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Long>?): Boolean {
            return size > config.maxSize
        }
    }

    // Statistics
    private val hits = AtomicLong(0)
    private val misses = AtomicLong(0)
    private val puts = AtomicLong(0)
    private val evictions = AtomicLong(0)

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: String): T? {
        val entry = cache[key]

        if (entry == null) {
            if (config.enableStats) misses.incrementAndGet()
            return null
        }

        // Check expiration
        if (entry.isExpired()) {
            remove(key)
            if (config.enableStats) misses.incrementAndGet()
            return null
        }

        // Update access order
        synchronized(accessOrder) {
            accessOrder[key] = System.currentTimeMillis()
        }

        if (config.enableStats) hits.incrementAndGet()
        return entry.value as? T
    }

    override fun <T : Any> put(key: String, value: T, ttl: Duration?) {
        val effectiveTtl = ttl ?: config.defaultTtl
        val now = Instant.now()
        val expiresAt = effectiveTtl?.let { now.plus(it) }

        val entry = CacheEntry(
            key = key,
            value = value,
            createdAt = now,
            expiresAt = expiresAt,
            ttl = effectiveTtl
        )

        // Check if we need to evict
        if (cache.size >= config.maxSize) {
            evictOne()
        }

        @Suppress("UNCHECKED_CAST")
        cache[key] = entry as CacheEntry<Any>
        synchronized(accessOrder) {
            accessOrder[key] = System.currentTimeMillis()
        }

        if (config.enableStats) puts.incrementAndGet()
    }

    override fun remove(key: String) {
        cache.remove(key)
        synchronized(accessOrder) {
            accessOrder.remove(key)
        }
    }

    override fun removePattern(pattern: String) {
        val regex = patternToRegex(pattern)
        val keysToRemove = cache.keys.filter { it.matches(regex) }
        keysToRemove.forEach { remove(it) }
    }

    override fun clear() {
        cache.clear()
        synchronized(accessOrder) {
            accessOrder.clear()
        }
    }

    override fun contains(key: String): Boolean {
        val entry = cache[key] ?: return false
        if (entry.isExpired()) {
            remove(key)
            return false
        }
        return true
    }

    override fun getStats(): CacheStats {
        return CacheStats(
            hits = hits.get(),
            misses = misses.get(),
            puts = puts.get(),
            evictions = evictions.get(),
            size = cache.size,
            maxSize = config.maxSize
        )
    }

    /**
     * Evict one entry based on eviction policy
     */
    private fun evictOne() {
        when (config.evictionPolicy) {
            EvictionPolicy.LRU -> evictLRU()
            EvictionPolicy.LFU -> evictLFU()
            EvictionPolicy.FIFO -> evictFIFO()
            EvictionPolicy.TTL -> evictExpired()
        }
    }

    /**
     * Evict least recently used entry
     */
    private fun evictLRU() {
        synchronized(accessOrder) {
            val lruKey = accessOrder.keys.firstOrNull()
            if (lruKey != null) {
                remove(lruKey)
                if (config.enableStats) evictions.incrementAndGet()
            }
        }
    }

    /**
     * Evict least frequently used entry
     * For simplicity, we use LRU as proxy for LFU
     */
    private fun evictLFU() {
        evictLRU()
    }

    /**
     * Evict oldest entry (FIFO)
     */
    private fun evictFIFO() {
        val oldestEntry = cache.entries
            .minByOrNull { it.value.createdAt }

        if (oldestEntry != null) {
            remove(oldestEntry.key)
            if (config.enableStats) evictions.incrementAndGet()
        }
    }

    /**
     * Evict expired entries
     */
    private fun evictExpired() {
        val now = Instant.now()
        val expiredKeys = cache.entries
            .filter { it.value.expiresAt?.isBefore(now) == true }
            .map { it.key }

        expiredKeys.forEach { remove(it) }

        if (config.enableStats) {
            evictions.addAndGet(expiredKeys.size.toLong())
        }
    }

    /**
     * Convert wildcard pattern to regex
     */
    private fun patternToRegex(pattern: String): Regex {
        val regexPattern = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".")
        return Regex(regexPattern)
    }

    /**
     * Clean up expired entries (can be called periodically)
     */
    fun cleanup() {
        val now = Instant.now()
        val expiredKeys = cache.entries
            .filter { it.value.expiresAt?.isBefore(now) == true }
            .map { it.key }

        expiredKeys.forEach { remove(it) }
    }
}
