// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/eviction/EvictionPolicy.kt

package com.korm.dsl.cache.eviction

import java.time.Duration

/**
 * Cache eviction policy.
 */
interface EvictionPolicy {
    /**
     * Check if entry should be evicted.
     */
    fun shouldEvict(
        createdAt: Long,
        lastAccessedAt: Long,
        accessCount: Long,
        expiresAt: Long?
    ): Boolean
}

/**
 * Time-based eviction policy.
 */
class TTLPolicy(private val ttl: Duration) : EvictionPolicy {
    override fun shouldEvict(
        createdAt: Long,
        lastAccessedAt: Long,
        accessCount: Long,
        expiresAt: Long?
    ): Boolean {
        val now = System.currentTimeMillis()
        return expiresAt != null && now > expiresAt
    }
}

/**
 * LRU-based eviction policy.
 */
class LRUPolicy(private val maxIdleTime: Duration) : EvictionPolicy {
    override fun shouldEvict(
        createdAt: Long,
        lastAccessedAt: Long,
        accessCount: Long,
        expiresAt: Long?
    ): Boolean {
        val now = System.currentTimeMillis()
        val idleTime = now - lastAccessedAt
        return idleTime > maxIdleTime.toMillis()
    }
}

/**
 * LFU-based eviction policy.
 */
class LFUPolicy(private val minAccessCount: Long) : EvictionPolicy {
    override fun shouldEvict(
        createdAt: Long,
        lastAccessedAt: Long,
        accessCount: Long,
        expiresAt: Long?
    ): Boolean {
        return accessCount < minAccessCount
    }
}

/**
 * Combined eviction policy.
 */
class CombinedPolicy(
    private val policies: List<EvictionPolicy>
) : EvictionPolicy {
    override fun shouldEvict(
        createdAt: Long,
        lastAccessedAt: Long,
        accessCount: Long,
        expiresAt: Long?
    ): Boolean {
        return policies.any {
            it.shouldEvict(createdAt, lastAccessedAt, accessCount, expiresAt)
        }
    }
}
