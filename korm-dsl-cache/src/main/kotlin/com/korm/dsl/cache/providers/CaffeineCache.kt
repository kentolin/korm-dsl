// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/providers/CaffeineCache.kt

package com.korm.dsl.cache.providers

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.Cache as CaffeineCache
import com.korm.dsl.cache.Cache
import com.korm.dsl.cache.CacheConfig
import com.korm.dsl.cache.CacheStats
import java.time.Duration

/**
 * Caffeine-based cache implementation.
 */
class CaffeineCacheProvider<K : Any, V : Any>(
    private val config: CacheConfig = CacheConfig()
) : Cache<K, V> {

    private val cache: CaffeineCache<K, V> = buildCache()

    private fun buildCache(): CaffeineCache<K, V> {
        var builder = Caffeine.newBuilder()
            .maximumSize(config.maxSize)

        config.expireAfterWrite?.let {
            builder = builder.expireAfterWrite(it)
        }

        config.expireAfterAccess?.let {
            builder = builder.expireAfterAccess(it)
        }

        if (config.recordStats) {
            builder = builder.recordStats()
        }

        return builder.build()
    }

    override fun get(key: K): V? {
        return cache.getIfPresent(key)
    }

    override fun put(key: K, value: V) {
        cache.put(key, value)
    }

    override fun put(key: K, value: V, ttl: Duration) {
        // Caffeine doesn't support per-entry TTL, use global config
        cache.put(key, value)
    }

    override fun remove(key: K) {
        cache.invalidate(key)
    }

    override fun containsKey(key: K): Boolean {
        return cache.getIfPresent(key) != null
    }

    override fun clear() {
        cache.invalidateAll()
    }

    override fun keys(): Set<K> {
        return cache.asMap().keys
    }

    override fun size(): Long {
        return cache.estimatedSize()
    }

    override fun stats(): CacheStats {
        val caffeineStats = cache.stats()

        return CacheStats(
            hitCount = caffeineStats.hitCount(),
            missCount = caffeineStats.missCount(),
            loadSuccessCount = caffeineStats.loadSuccessCount(),
            loadFailureCount = caffeineStats.loadFailureCount(),
            totalLoadTime = caffeineStats.totalLoadTime(),
            evictionCount = caffeineStats.evictionCount(),
            size = cache.estimatedSize()
        )
    }
}
