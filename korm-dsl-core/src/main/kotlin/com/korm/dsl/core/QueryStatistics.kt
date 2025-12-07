// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/QueryStatistics.kt

package com.korm.dsl.core

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Query execution statistics.
 */
data class QueryStats(
    val query: String,
    val executionCount: Long,
    val totalExecutionTimeMs: Long,
    val avgExecutionTimeMs: Long,
    val minExecutionTimeMs: Long,
    val maxExecutionTimeMs: Long,
    val lastExecutionTimeMs: Long
)

/**
 * Query statistics collector.
 */
class QueryStatisticsCollector {
    private data class Stats(
        val count: AtomicLong = AtomicLong(0),
        val totalTime: AtomicLong = AtomicLong(0),
        val minTime: AtomicLong = AtomicLong(Long.MAX_VALUE),
        val maxTime: AtomicLong = AtomicLong(0),
        val lastTime: AtomicLong = AtomicLong(0)
    )

    private val stats = ConcurrentHashMap<String, Stats>()

    /**
     * Record query execution.
     */
    fun recordExecution(query: String, executionTimeMs: Long) {
        val queryStats = stats.computeIfAbsent(query) { Stats() }

        queryStats.count.incrementAndGet()
        queryStats.totalTime.addAndGet(executionTimeMs)
        queryStats.lastTime.set(executionTimeMs)

        // Update min time
        var currentMin = queryStats.minTime.get()
        while (executionTimeMs < currentMin) {
            if (queryStats.minTime.compareAndSet(currentMin, executionTimeMs)) {
                break
            }
            currentMin = queryStats.minTime.get()
        }

        // Update max time
        var currentMax = queryStats.maxTime.get()
        while (executionTimeMs > currentMax) {
            if (queryStats.maxTime.compareAndSet(currentMax, executionTimeMs)) {
                break
            }
            currentMax = queryStats.maxTime.get()
        }
    }

    /**
     * Get statistics for a specific query.
     */
    fun getStats(query: String): QueryStats? {
        val queryStats = stats[query] ?: return null

        val count = queryStats.count.get()
        val totalTime = queryStats.totalTime.get()

        return QueryStats(
            query = query,
            executionCount = count,
            totalExecutionTimeMs = totalTime,
            avgExecutionTimeMs = if (count > 0) totalTime / count else 0,
            minExecutionTimeMs = queryStats.minTime.get(),
            maxExecutionTimeMs = queryStats.maxTime.get(),
            lastExecutionTimeMs = queryStats.lastTime.get()
        )
    }

    /**
     * Get all query statistics.
     */
    fun getAllStats(): List<QueryStats> {
        return stats.keys.mapNotNull { getStats(it) }
    }

    /**
     * Get top N slowest queries.
     */
    fun getSlowestQueries(n: Int = 10): List<QueryStats> {
        return getAllStats()
            .sortedByDescending { it.avgExecutionTimeMs }
            .take(n)
    }

    /**
     * Get top N most frequently executed queries.
     */
    fun getMostFrequentQueries(n: Int = 10): List<QueryStats> {
        return getAllStats()
            .sortedByDescending { it.executionCount }
            .take(n)
    }

    /**
     * Clear all statistics.
     */
    fun clear() {
        stats.clear()
    }

    /**
     * Clear statistics for a specific query.
     */
    fun clear(query: String) {
        stats.remove(query)
    }
}
