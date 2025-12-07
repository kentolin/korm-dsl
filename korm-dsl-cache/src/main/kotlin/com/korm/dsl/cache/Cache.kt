// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/Cache.kt

package com.korm.dsl.cache

import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Cache interface for storing and retrieving data.
 */
interface Cache<K, V> {
    /**
     * Get value from cache.
     */
    fun get(key: K): V?

    /**
     * Put value into cache.
     */
    fun put(key: K, value: V)

    /**
     * Put value with expiration time.
     */
    fun put(key: K, value: V, ttl: Duration)

    /**
     * Remove value from cache.
     */
    fun remove(key: K)

    /**
     * Check if key exists in cache.
     */
    fun containsKey(key: K): Boolean

    /**
     * Clear all entries from cache.
     */
    fun clear()

    /**
     * Get all keys in cache.
     */
    fun keys(): Set<K>

    /**
     * Get cache size.
     */
    fun size(): Long

    /**
     * Get cache statistics.
     */
    fun stats(): CacheStats

    /**
     * Get or compute value if absent.
     */
    fun getOrPut(key: K, supplier: () -> V): V {
        return get(key) ?: supplier().also { put(key, it) }
    }

    /**
     * Get or compute value with TTL if absent.
     */
    fun getOrPut(key: K, ttl: Duration, supplier: () -> V): V {
        return get(key) ?: supplier().also { put(key, it, ttl) }
    }
}

/**
 * Cache statistics.
 */
data class CacheStats(
    val hitCount: Long = 0,
    val missCount: Long = 0,
    val loadSuccessCount: Long = 0,
    val loadFailureCount: Long = 0,
    val totalLoadTime: Long = 0,
    val evictionCount: Long = 0,
    val size: Long = 0
) {
    val hitRate: Double
        get() = if (requestCount > 0) hitCount.toDouble() / requestCount else 0.0

    val missRate: Double
        get() = if (requestCount > 0) missCount.toDouble() / requestCount else 0.0

    val requestCount: Long
        get() = hitCount + missCount

    val averageLoadPenalty: Double
        get() = if (loadSuccessCount > 0) totalLoadTime.toDouble() / loadSuccessCount else 0.0
}

/**
 * Cache entry with metadata.
 */
data class CacheEntry<V>(
    val value: V,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,
    val accessCount: Long = 0,
    val lastAccessedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if entry is expired.
     */
    fun isExpired(): Boolean {
        return expiresAt != null && System.currentTimeMillis() > expiresAt
    }

    /**
     * Create a copy with updated access info.
     */
    fun accessed(): CacheEntry<V> {
        return copy(
            accessCount = accessCount + 1,
            lastAccessedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Cache event types.
 */
enum class CacheEventType {
    CREATED,
    UPDATED,
    REMOVED,
    EXPIRED,
    EVICTED
}

/**
 * Cache event.
 */
data class CacheEvent<K, V>(
    val type: CacheEventType,
    val key: K,
    val oldValue: V? = null,
    val newValue: V? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Cache event listener.
 */
interface CacheEventListener<K, V> {
    fun onEvent(event: CacheEvent<K, V>)
}
