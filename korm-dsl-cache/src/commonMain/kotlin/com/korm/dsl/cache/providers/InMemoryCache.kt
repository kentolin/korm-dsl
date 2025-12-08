// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/providers/InMemoryCache.kt

package com.korm.dsl.cache.providers

import com.korm.dsl.cache.*
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * In-memory cache implementation.
 */
class InMemoryCache<K, V>(
    private val config: CacheConfig = CacheConfig()
) : Cache<K, V> {

    private val storage = ConcurrentHashMap<K, CacheEntry<V>>()
    private val evictionStrategy: EvictionStrategy<K> = createEvictionStrategy(config.evictionStrategy)

    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)
    private val evictionCount = AtomicLong(0)

    override fun get(key: K): V? {
        cleanupExpired()

        val entry = storage[key]

        if (entry == null) {
            missCount.incrementAndGet()
            return null
        }

        if (entry.isExpired()) {
            storage.remove(key)
            evictionStrategy.remove(key)
            missCount.incrementAndGet()
            return null
        }

        // Update access info
        storage[key] = entry.accessed()
        evictionStrategy.recordAccess(key)

        hitCount.incrementAndGet()
        return entry.value
    }

    override fun put(key: K, value: V) {
        put(key, value, config.defaultTTL ?: Duration.ofHours(1))
    }

    override fun put(key: K, value: V, ttl: Duration) {
        cleanupExpired()
        ensureCapacity()

        val expiresAt = System.currentTimeMillis() + ttl.toMillis()
        val entry = CacheEntry(value, expiresAt = expiresAt)

        storage[key] = entry
        evictionStrategy.recordAccess(key)
    }

    override fun remove(key: K) {
        storage.remove(key)
        evictionStrategy.remove(key)
    }

    override fun containsKey(key: K): Boolean {
        cleanupExpired()
        val entry = storage[key]
        return entry != null && !entry.isExpired()
    }

    override fun clear() {
        storage.clear()
        evictionStrategy.clear()
        resetStats()
    }

    override fun keys(): Set<K> {
        cleanupExpired()
        return storage.keys.toSet()
    }

    override fun size(): Long {
        cleanupExpired()
        return storage.size.toLong()
    }

    override fun stats(): CacheStats {
        return CacheStats(
            hitCount = hitCount.get(),
            missCount = missCount.get(),
            evictionCount = evictionCount.get(),
            size = storage.size.toLong()
        )
    }

    /**
     * Ensure cache doesn't exceed max size.
     */
    private fun ensureCapacity() {
        while (storage.size >= config.maxSize) {
            val keyToEvict = evictionStrategy.getEvictionCandidate()
            if (keyToEvict != null) {
                storage.remove(keyToEvict)
                evictionStrategy.remove(keyToEvict)
                evictionCount.incrementAndGet()
            } else {
                break
            }
        }
    }

    /**
     * Remove expired entries.
     */
    private fun cleanupExpired() {
        val now = System.currentTimeMillis()
        val keysToRemove = mutableListOf<K>()

        storage.forEach { (key, entry) ->
            if (entry.expiresAt != null && now > entry.expiresAt) {
                keysToRemove.add(key)
            }
        }

        keysToRemove.forEach { key ->
            storage.remove(key)
            evictionStrategy.remove(key)
        }
    }

    /**
     * Reset statistics.
     */
    private fun resetStats() {
        hitCount.set(0)
        missCount.set(0)
        evictionCount.set(0)
    }
}
