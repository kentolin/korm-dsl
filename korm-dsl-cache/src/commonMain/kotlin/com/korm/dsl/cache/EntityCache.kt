// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/EntityCache.kt

package com.korm.dsl.cache

import java.time.Duration
import kotlin.reflect.KClass

/**
 * Cache specifically for database entities.
 */
class EntityCache<ID : Any, E : Any>(
    private val entityClass: KClass<E>,
    private val config: CacheConfig = CacheConfig()
) {
    private val cache: Cache<ID, E> = InMemoryCache(config)

    /**
     * Get entity by ID.
     */
    fun get(id: ID): E? {
        return cache.get(id)
    }

    /**
     * Put entity into cache.
     */
    fun put(id: ID, entity: E) {
        cache.put(id, entity)
    }

    /**
     * Put entity with custom TTL.
     */
    fun put(id: ID, entity: E, ttl: Duration) {
        cache.put(id, entity, ttl)
    }

    /**
     * Remove entity from cache.
     */
    fun evict(id: ID) {
        cache.remove(id)
    }

    /**
     * Get entity or load if not cached.
     */
    fun getOrLoad(id: ID, loader: (ID) -> E?): E? {
        return cache.get(id) ?: loader(id)?.also { put(id, it) }
    }

    /**
     * Invalidate all entities of this type.
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

    /**
     * Get entity class.
     */
    fun getEntityClass(): KClass<E> = entityClass
}

/**
 * Multi-level entity cache.
 */
class MultiLevelEntityCache<ID : Any, E : Any>(
    private val entityClass: KClass<E>,
    private val l1Cache: Cache<ID, E>,
    private val l2Cache: Cache<ID, E>? = null
) {

    /**
     * Get entity from cache (checks L1, then L2).
     */
    fun get(id: ID): E? {
        // Try L1 first
        val l1Value = l1Cache.get(id)
        if (l1Value != null) return l1Value

        // Try L2
        val l2Value = l2Cache?.get(id)
        if (l2Value != null) {
            // Promote to L1
            l1Cache.put(id, l2Value)
            return l2Value
        }

        return null
    }

    /**
     * Put entity into both cache levels.
     */
    fun put(id: ID, entity: E) {
        l1Cache.put(id, entity)
        l2Cache?.put(id, entity)
    }

    /**
     * Remove entity from both cache levels.
     */
    fun evict(id: ID) {
        l1Cache.remove(id)
        l2Cache?.remove(id)
    }

    /**
     * Clear all cache levels.
     */
    fun clear() {
        l1Cache.clear()
        l2Cache?.clear()
    }
}
