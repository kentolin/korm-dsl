// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/CacheStrategy.kt

package com.korm.dsl.cache

import java.time.Duration

/**
 * Cache eviction strategy.
 */
interface EvictionStrategy<K> {
    /**
     * Record access to a key.
     */
    fun recordAccess(key: K)

    /**
     * Get key to evict.
     */
    fun getEvictionCandidate(): K?

    /**
     * Remove key from tracking.
     */
    fun remove(key: K)

    /**
     * Clear all tracking.
     */
    fun clear()
}

/**
 * Least Recently Used (LRU) eviction strategy.
 */
class LRUEvictionStrategy<K> : EvictionStrategy<K> {
    private val accessOrder = LinkedHashMap<K, Long>()

    override fun recordAccess(key: K) {
        synchronized(accessOrder) {
            accessOrder.remove(key)
            accessOrder[key] = System.currentTimeMillis()
        }
    }

    override fun getEvictionCandidate(): K? {
        synchronized(accessOrder) {
            return accessOrder.keys.firstOrNull()
        }
    }

    override fun remove(key: K) {
        synchronized(accessOrder) {
            accessOrder.remove(key)
        }
    }

    override fun clear() {
        synchronized(accessOrder) {
            accessOrder.clear()
        }
    }
}

/**
 * Least Frequently Used (LFU) eviction strategy.
 */
class LFUEvictionStrategy<K> : EvictionStrategy<K> {
    private val frequencies = mutableMapOf<K, Long>()

    override fun recordAccess(key: K) {
        synchronized(frequencies) {
            frequencies[key] = (frequencies[key] ?: 0) + 1
        }
    }

    override fun getEvictionCandidate(): K? {
        synchronized(frequencies) {
            return frequencies.minByOrNull { it.value }?.key
        }
    }

    override fun remove(key: K) {
        synchronized(frequencies) {
            frequencies.remove(key)
        }
    }

    override fun clear() {
        synchronized(frequencies) {
            frequencies.clear()
        }
    }
}

/**
 * First In First Out (FIFO) eviction strategy.
 */
class FIFOEvictionStrategy<K> : EvictionStrategy<K> {
    private val insertionOrder = LinkedHashMap<K, Long>()

    override fun recordAccess(key: K) {
        synchronized(insertionOrder) {
            if (!insertionOrder.containsKey(key)) {
                insertionOrder[key] = System.currentTimeMillis()
            }
        }
    }

    override fun getEvictionCandidate(): K? {
        synchronized(insertionOrder) {
            return insertionOrder.keys.firstOrNull()
        }
    }

    override fun remove(key: K) {
        synchronized(insertionOrder) {
            insertionOrder.remove(key)
        }
    }

    override fun clear() {
        synchronized(insertionOrder) {
            insertionOrder.clear()
        }
    }
}

/**
 * Cache configuration.
 */
data class CacheConfig(
    val maxSize: Long = 10000,
    val defaultTTL: Duration? = null,
    val evictionStrategy: EvictionStrategyType = EvictionStrategyType.LRU,
    val recordStats: Boolean = true,
    val expireAfterWrite: Duration? = null,
    val expireAfterAccess: Duration? = null,
    val refreshAfterWrite: Duration? = null
)

/**
 * Eviction strategy types.
 */
enum class EvictionStrategyType {
    LRU,
    LFU,
    FIFO
}

/**
 * Create eviction strategy based on type.
 */
fun <K> createEvictionStrategy(type: EvictionStrategyType): EvictionStrategy<K> {
    return when (type) {
        EvictionStrategyType.LRU -> LRUEvictionStrategy()
        EvictionStrategyType.LFU -> LFUEvictionStrategy()
        EvictionStrategyType.FIFO -> FIFOEvictionStrategy()
    }
}
