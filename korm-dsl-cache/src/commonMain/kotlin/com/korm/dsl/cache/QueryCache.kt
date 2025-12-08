// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/QueryCache.kt

package com.korm.dsl.cache

import java.security.MessageDigest
import java.time.Duration

/**
 * Cache for query results.
 */
class QueryCache<T>(
    private val config: CacheConfig = CacheConfig()
) {
    private val cache: Cache<String, List<T>> = InMemoryCache(config)

    /**
     * Generate cache key from query.
     */
    private fun generateKey(query: String, params: Map<String, Any?>): String {
        val combined = "$query:${params.entries.sortedBy { it.key }.joinToString(",") { "${it.key}=${it.value}" }}"
        val bytes = MessageDigest.getInstance("MD5").digest(combined.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Get cached query result.
     */
    fun get(query: String, params: Map<String, Any?> = emptyMap()): List<T>? {
        val key = generateKey(query, params)
        return cache.get(key)
    }

    /**
     * Put query result into cache.
     */
    fun put(query: String, params: Map<String, Any?> = emptyMap(), result: List<T>) {
        val key = generateKey(query, params)
        cache.put(key, result)
    }

    /**
     * Put query result with custom TTL.
     */
    fun put(query: String, params: Map<String, Any?> = emptyMap(), result: List<T>, ttl: Duration) {
        val key = generateKey(query, params)
        cache.put(key, result, ttl)
    }

    /**
     * Execute query and cache result.
     */
    fun executeAndCache(
        query: String,
        params: Map<String, Any?> = emptyMap(),
        ttl: Duration? = null,
        executor: () -> List<T>
    ): List<T> {
        val key = generateKey(query, params)

        return cache.get(key) ?: executor().also { result ->
            if (ttl != null) {
                cache.put(key, result, ttl)
            } else {
                cache.put(key, result)
            }
        }
    }

    /**
     * Invalidate specific query.
     */
    fun invalidate(query: String, params: Map<String, Any?> = emptyMap()) {
        val key = generateKey(query, params)
        cache.remove(key)
    }

    /**
     * Clear all cached queries.
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Get cache statistics.
     */
    fun stats(): CacheStats {
        return cache.stats()
    }
}

/**
 * Query cache key.
 */
data class QueryCacheKey(
    val sql: String,
    val parameters: Map<String, Any?>,
    val hash: String
) {
    companion object {
        fun create(sql: String, parameters: Map<String, Any?>): QueryCacheKey {
            val combined = "$sql:${parameters.entries.sortedBy { it.key }.joinToString(",") { "${it.key}=${it.value}" }}"
            val bytes = MessageDigest.getInstance("MD5").digest(combined.toByteArray())
            val hash = bytes.joinToString("") { "%02x".format(it) }

            return QueryCacheKey(sql, parameters, hash)
        }
    }
}
