package com.korm.dsl.cache

import com.korm.dsl.core.Database
import java.sql.ResultSet
import java.time.Duration

/**
 * Cache manager for query result caching
 */
class CacheManager(
    private val cache: Cache = InMemoryCache(),
    private val config: CacheManagerConfig = CacheManagerConfig()
) {

    /**
     * Execute query with caching
     */
    fun <T> executeWithCache(
        db: Database,
        sql: String,
        parameters: List<Any?> = emptyList(),
        ttl: Duration? = null,
        mapper: (ResultSet) -> T
    ): List<T> {
        // Build cache key
        val cacheKey = CacheKeyBuilder.buildKey(sql, parameters)

        // Try to get from cache
        val cached = cache.get<List<T>>(cacheKey)
        if (cached != null && config.enableQueryCache) {
            return cached
        }

        // Execute query
        val results = mutableListOf<T>()
        db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                // Set parameters
                parameters.forEachIndexed { index, param ->
                    stmt.setObject(index + 1, param)
                }

                val rs = stmt.executeQuery()
                while (rs.next()) {
                    results.add(mapper(rs))
                }
            }
        }

        // Store in cache
        if (config.enableQueryCache) {
            cache.put(cacheKey, results, ttl ?: config.defaultQueryTtl)
        }

        return results
    }

    /**
     * Invalidate cache for a table
     */
    fun invalidateTable(tableName: String) {
        when (config.invalidationStrategy) {
            is TableInvalidationStrategy.Pattern -> {
                val pattern = CacheKeyBuilder.buildTablePattern(tableName)
                cache.removePattern(pattern)
            }
            is TableInvalidationStrategy.All -> {
                cache.clear()
            }
            is TableInvalidationStrategy.None -> {
                // Do nothing
            }
        }
    }

    /**
     * Invalidate cache by key
     */
    fun invalidate(key: String) {
        cache.remove(key)
    }

    /**
     * Invalidate cache by pattern
     */
    fun invalidatePattern(pattern: String) {
        cache.removePattern(pattern)
    }

    /**
     * Clear all cache
     */
    fun clear() {
        cache.clear()
    }

    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats {
        return cache.getStats()
    }

    /**
     * Print formatted statistics
     */
    fun printStats() {
        val stats = getStats()
        println("""
            ╔════════════════════════════════════════════════════════════════╗
            ║                    Cache Statistics                           ║
            ╚════════════════════════════════════════════════════════════════╝

            Hits:          ${stats.hits}
            Misses:        ${stats.misses}
            Hit Rate:      ${String.format("%.2f%%", stats.hitRate * 100)}
            Miss Rate:     ${String.format("%.2f%%", stats.missRate * 100)}

            Puts:          ${stats.puts}
            Evictions:     ${stats.evictions}

            Size:          ${stats.size}/${stats.maxSize}
            Fill Rate:     ${String.format("%.2f%%", (stats.size.toDouble() / stats.maxSize) * 100)}

            ════════════════════════════════════════════════════════════════
        """.trimIndent())
    }

    companion object {
        /**
         * Global cache manager instance
         */
        private var globalInstance: CacheManager? = null

        /**
         * Get or create global cache manager
         */
        fun global(
            cache: Cache = InMemoryCache(),
            config: CacheManagerConfig = CacheManagerConfig()
        ): CacheManager {
            if (globalInstance == null) {
                globalInstance = CacheManager(cache, config)
            }
            return globalInstance!!
        }

        /**
         * Configure global cache manager
         */
        fun configure(
            cache: Cache = InMemoryCache(),
            config: CacheManagerConfig = CacheManagerConfig()
        ) {
            globalInstance = CacheManager(cache, config)
        }

        /**
         * Reset global cache manager
         */
        fun reset() {
            globalInstance?.clear()
            globalInstance = null
        }
    }
}

/**
 * Cache manager configuration
 */
data class CacheManagerConfig(
    /**
     * Enable query result caching
     */
    val enableQueryCache: Boolean = true,

    /**
     * Default TTL for query results
     */
    val defaultQueryTtl: Duration = Duration.ofMinutes(30),

    /**
     * Invalidation strategy for tables
     */
    val invalidationStrategy: TableInvalidationStrategy = TableInvalidationStrategy.Pattern,

    /**
     * Cache write operations (INSERT, UPDATE, DELETE)
     */
    val cacheWrites: Boolean = false
)

/**
 * Table invalidation strategies
 */
sealed class TableInvalidationStrategy {
    /**
     * Invalidate by pattern matching
     */
    object Pattern : TableInvalidationStrategy()

    /**
     * Invalidate entire cache
     */
    object All : TableInvalidationStrategy()

    /**
     * Don't invalidate (manual only)
     */
    object None : TableInvalidationStrategy()
}

/**
 * Cached query result wrapper
 */
data class CachedResult<T>(
    val data: List<T>,
    val cacheKey: String,
    val fromCache: Boolean,
    val executionTimeMs: Long? = null
)

/**
 * DSL for cache configuration
 */
class CacheManagerConfigBuilder {
    var enableQueryCache: Boolean = true
    var defaultQueryTtl: Duration = Duration.ofMinutes(30)
    var invalidationStrategy: TableInvalidationStrategy = TableInvalidationStrategy.Pattern
    var cacheWrites: Boolean = false

    fun build(): CacheManagerConfig {
        return CacheManagerConfig(
            enableQueryCache = enableQueryCache,
            defaultQueryTtl = defaultQueryTtl,
            invalidationStrategy = invalidationStrategy,
            cacheWrites = cacheWrites
        )
    }
}

/**
 * Create cache manager configuration using DSL
 */
fun cacheManagerConfig(block: CacheManagerConfigBuilder.() -> Unit): CacheManagerConfig {
    val builder = CacheManagerConfigBuilder()
    builder.block()
    return builder.build()
}
