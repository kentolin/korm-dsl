// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/CacheManager.kt

package com.korm.dsl.cache

import java.util.concurrent.ConcurrentHashMap

/**
 * Manages multiple named caches.
 */
class CacheManager {
    private val caches = ConcurrentHashMap<String, Cache<*, *>>()

    /**
     * Get or create a cache.
     */
    @Suppress("UNCHECKED_CAST")
    fun <K, V> getCache(
        name: String,
        config: CacheConfig = CacheConfig()
    ): Cache<K, V> {
        return caches.getOrPut(name) {
            InMemoryCache<K, V>(config)
        } as Cache<K, V>
    }

    /**
     * Register a cache.
     */
    fun <K, V> registerCache(name: String, cache: Cache<K, V>) {
        caches[name] = cache
    }

    /**
     * Remove a cache.
     */
    fun removeCache(name: String) {
        caches[name]?.clear()
        caches.remove(name)
    }

    /**
     * Get all cache names.
     */
    fun getCacheNames(): Set<String> {
        return caches.keys.toSet()
    }

    /**
     * Clear all caches.
     */
    fun clearAll() {
        caches.values.forEach { it.clear() }
    }

    /**
     * Get statistics for all caches.
     */
    fun getAllStats(): Map<String, CacheStats> {
        return caches.mapValues { (_, cache) -> cache.stats() }
    }

    /**
     * Get total statistics across all caches.
     */
    fun getTotalStats(): CacheStats {
        val allStats = caches.values.map { it.stats() }

        return CacheStats(
            hitCount = allStats.sumOf { it.hitCount },
            missCount = allStats.sumOf { it.missCount },
            loadSuccessCount = allStats.sumOf { it.loadSuccessCount },
            loadFailureCount = allStats.sumOf { it.loadFailureCount },
            totalLoadTime = allStats.sumOf { it.totalLoadTime },
            evictionCount = allStats.sumOf { it.evictionCount },
            size = allStats.sumOf { it.size }
        )
    }
}

/**
 * Global cache manager instance.
 */
object GlobalCacheManager {
    private val manager = CacheManager()

    fun <K, V> getCache(name: String, config: CacheConfig = CacheConfig()): Cache<K, V> {
        return manager.getCache(name, config)
    }

    fun <K, V> registerCache(name: String, cache: Cache<K, V>) {
        manager.registerCache(name, cache)
    }

    fun clearAll() {
        manager.clearAll()
    }

    fun getAllStats(): Map<String, CacheStats> {
        return manager.getAllStats()
    }
}
