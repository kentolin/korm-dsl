package com.korm.dsl.cache

import java.time.Duration
import java.time.Instant

/**
 * Cache provider interface
 */
interface Cache {
    /**
     * Get value from cache
     */
    fun <T : Any> get(key: String): T?

    /**
     * Put value in cache with optional TTL
     */
    fun <T : Any> put(key: String, value: T, ttl: Duration? = null)

    /**
     * Remove value from cache
     */
    fun remove(key: String)

    /**
     * Remove all entries matching a pattern
     */
    fun removePattern(pattern: String)

    /**
     * Clear entire cache
     */
    fun clear()

    /**
     * Check if key exists in cache
     */
    fun contains(key: String): Boolean

    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats
}

/**
 * Cache entry with metadata
 */
data class CacheEntry<T : Any>(
    val key: String,
    val value: T,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val ttl: Duration?
) {
    /**
     * Check if entry is expired
     */
    fun isExpired(): Boolean {
        return expiresAt?.let { Instant.now().isAfter(it) } ?: false
    }

    /**
     * Get remaining TTL
     */
    fun remainingTtl(): Duration? {
        return expiresAt?.let { Duration.between(Instant.now(), it) }
    }
}

/**
 * Cache statistics
 */
data class CacheStats(
    val hits: Long = 0,
    val misses: Long = 0,
    val puts: Long = 0,
    val evictions: Long = 0,
    val size: Int = 0,
    val maxSize: Int = 0
) {
    val hitRate: Double
        get() = if (hits + misses > 0) hits.toDouble() / (hits + misses) else 0.0

    val missRate: Double
        get() = if (hits + misses > 0) misses.toDouble() / (hits + misses) else 0.0

    override fun toString(): String {
        return """
            CacheStats(
              hits=$hits,
              misses=$misses,
              hitRate=${String.format("%.2f%%", hitRate * 100)},
              puts=$puts,
              evictions=$evictions,
              size=$size/$maxSize
            )
        """.trimIndent()
    }
}

/**
 * Cache configuration
 */
data class CacheConfig(
    /**
     * Maximum number of entries
     */
    val maxSize: Int = 1000,

    /**
     * Default TTL for entries
     */
    val defaultTtl: Duration? = Duration.ofMinutes(30),

    /**
     * Enable statistics tracking
     */
    val enableStats: Boolean = true,

    /**
     * Eviction policy
     */
    val evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
)

/**
 * Cache eviction policies
 */
enum class EvictionPolicy {
    /**
     * Least Recently Used - evict least recently accessed entry
     */
    LRU,

    /**
     * Least Frequently Used - evict least frequently accessed entry
     */
    LFU,

    /**
     * First In First Out - evict oldest entry
     */
    FIFO,

    /**
     * Time To Live - evict expired entries
     */
    TTL
}

/**
 * Cache key builder for queries
 */
object CacheKeyBuilder {
    /**
     * Build cache key from SQL and parameters
     */
    fun buildKey(sql: String, parameters: List<Any?>): String {
        val normalizedSql = sql.trim().replace(Regex("\\s+"), " ")
        val paramsHash = parameters.joinToString(",") { it?.toString() ?: "null" }
        return "query:${normalizedSql.hashCode()}:${paramsHash.hashCode()}"
    }

    /**
     * Build cache key for table
     */
    fun buildTableKey(tableName: String): String {
        return "table:$tableName"
    }

    /**
     * Build pattern for table queries
     */
    fun buildTablePattern(tableName: String): String {
        return "query:*$tableName*"
    }
}

/**
 * Cache invalidation strategy
 */
sealed class InvalidationStrategy {
    /**
     * Invalidate all cache
     */
    object All : InvalidationStrategy()

    /**
     * Invalidate by key
     */
    data class ByKey(val key: String) : InvalidationStrategy()

    /**
     * Invalidate by pattern
     */
    data class ByPattern(val pattern: String) : InvalidationStrategy()

    /**
     * Invalidate by table name
     */
    data class ByTable(val tableName: String) : InvalidationStrategy()

    /**
     * Invalidate by tags
     */
    data class ByTags(val tags: Set<String>) : InvalidationStrategy()
}
