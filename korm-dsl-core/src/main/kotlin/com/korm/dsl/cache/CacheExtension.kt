package com.korm.dsl.cache

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Table
import java.sql.ResultSet
import java.time.Duration

/**
 * Database extensions for cached queries
 */

/**
 * Execute cached query
 */
fun <T> Database.cachedQuery(
    sql: String,
    parameters: List<Any?> = emptyList(),
    ttl: Duration? = null,
    cacheManager: CacheManager = CacheManager.global(),
    mapper: (ResultSet) -> T
): List<T> {
    return cacheManager.executeWithCache(this, sql, parameters, ttl, mapper)
}

/**
 * Invalidate cache for table
 */
fun Database.invalidateCache(
    tableName: String,
    cacheManager: CacheManager = CacheManager.global()
) {
    cacheManager.invalidateTable(tableName)
}

/**
 * Invalidate cache for table
 */
fun Table.invalidateCache(
    db: Database,
    cacheManager: CacheManager = CacheManager.global()
) {
    cacheManager.invalidateTable(this.tableName)
}

/**
 * Clear all cache
 */
fun Database.clearCache(cacheManager: CacheManager = CacheManager.global()) {
    cacheManager.clear()
}

/**
 * Get cache statistics
 */
fun Database.cacheStats(cacheManager: CacheManager = CacheManager.global()): CacheStats {
    return cacheManager.getStats()
}

/**
 * Print cache statistics
 */
fun Database.printCacheStats(cacheManager: CacheManager = CacheManager.global()) {
    cacheManager.printStats()
}

/**
 * Cacheable annotation for marking queries that should be cached
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable(
    /**
     * Cache key pattern
     */
    val key: String = "",

    /**
     * TTL in seconds
     */
    val ttl: Long = 1800, // 30 minutes default

    /**
     * Condition for caching (SpEL-like expression)
     */
    val condition: String = ""
)

/**
 * Cache evict annotation for marking operations that should invalidate cache
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheEvict(
    /**
     * Cache key pattern to evict
     */
    val key: String = "",

    /**
     * Table names to invalidate
     */
    val tables: Array<String> = [],

    /**
     * Whether to evict all cache
     */
    val allEntries: Boolean = false
)
