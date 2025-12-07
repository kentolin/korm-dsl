// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/CacheableRepository.kt

package com.korm.dsl.cache

import java.time.Duration

/**
 * Repository with caching capabilities.
 */
abstract class CacheableRepository<ID : Any, E : Any> {

    protected abstract val entityCache: EntityCache<ID, E>
    protected abstract val queryCache: QueryCache<E>

    /**
     * Find by ID with caching.
     */
    protected fun findByIdCached(id: ID, loader: (ID) -> E?): E? {
        return entityCache.getOrLoad(id, loader)
    }

    /**
     * Execute query with caching.
     */
    protected fun executeQueryCached(
        query: String,
        params: Map<String, Any?> = emptyMap(),
        ttl: Duration? = null,
        executor: () -> List<E>
    ): List<E> {
        return queryCache.executeAndCache(query, params, ttl, executor)
    }

    /**
     * Save entity and update cache.
     */
    protected fun saveAndCache(id: ID, entity: E, saver: (E) -> E): E {
        val saved = saver(entity)
        entityCache.put(id, saved)
        queryCache.clear() // Invalidate query cache
        return saved
    }

    /**
     * Delete entity and evict from cache.
     */
    protected fun deleteAndEvict(id: ID, deleter: (ID) -> Unit) {
        deleter(id)
        entityCache.evict(id)
        queryCache.clear() // Invalidate query cache
    }

    /**
     * Clear all caches.
     */
    fun clearCaches() {
        entityCache.clear()
        queryCache.clear()
    }

    /**
     * Get cache statistics.
     */
    fun getCacheStats(): Map<String, CacheStats> {
        return mapOf(
            "entity" to entityCache.stats(),
            "query" to queryCache.stats()
        )
    }
}
