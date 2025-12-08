// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/QueryMonitor.kt

package com.korm.dsl.monitoring

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Monitor for tracking query performance.
 */
class QueryMonitor(
    private val slowQueryThresholdMs: Long = 1000
) {
    private val logger = LoggerFactory.getLogger(QueryMonitor::class.java)

    private val queryStats = ConcurrentHashMap<String, QueryStats>()
    private val slowQueries = mutableListOf<SlowQuery>()

    /**
     * Record query execution.
     */
    fun recordQuery(
        sql: String,
        parameters: Map<String, Any?> = emptyMap(),
        durationMs: Long,
        success: Boolean = true
    ) {
        // Update stats
        val stats = queryStats.getOrPut(sql) { QueryStats(sql) }
        stats.record(durationMs, success)

        // Log slow queries
        if (durationMs >= slowQueryThresholdMs) {
            val slowQuery = SlowQuery(
                sql = sql,
                parameters = parameters,
                durationMs = durationMs,
                timestamp = System.currentTimeMillis()
            )

            synchronized(slowQueries) {
                slowQueries.add(slowQuery)
                // Keep only last 100 slow queries
                if (slowQueries.size > 100) {
                    slowQueries.removeAt(0)
                }
            }

            logger.warn("Slow query detected (${durationMs}ms): $sql")
        }
    }

    /**
     * Get stats for a specific query.
     */
    fun getQueryStats(sql: String): QueryStats? {
        return queryStats[sql]
    }

    /**
     * Get all query stats.
     */
    fun getAllStats(): List<QueryStats> {
        return queryStats.values.toList()
    }

    /**
     * Get top N slowest queries.
     */
    fun getSlowestQueries(n: Int = 10): List<QueryStats> {
        return queryStats.values
            .sortedByDescending { it.maxDuration }
            .take(n)
    }

    /**
     * Get most frequently executed queries.
     */
    fun getMostFrequentQueries(n: Int = 10): List<QueryStats> {
        return queryStats.values
            .sortedByDescending { it.executionCount }
            .take(n)
    }

    /**
     * Get recent slow queries.
     */
    fun getSlowQueries(limit: Int = 10): List<SlowQuery> {
        synchronized(slowQueries) {
            return slowQueries.takeLast(limit)
        }
    }

    /**
     * Clear all statistics.
     */
    fun clear() {
        queryStats.clear()
        synchronized(slowQueries) {
            slowQueries.clear()
        }
    }
}

/**
 * Statistics for a specific query.
 */
class QueryStats(val sql: String) {
    private val executions = AtomicLong(0)
    private val totalDuration = AtomicLong(0)
    private val errors = AtomicLong(0)

    @Volatile
    var maxDuration: Long = 0
        private set

    @Volatile
    var minDuration: Long = Long.MAX_VALUE
        private set

    val executionCount: Long
        get() = executions.get()

    val errorCount: Long
        get() = errors.get()

    val averageDuration: Double
        get() {
            val count = executions.get()
            return if (count > 0) totalDuration.get().toDouble() / count else 0.0
        }

    fun record(durationMs: Long, success: Boolean) {
        executions.incrementAndGet()
        totalDuration.addAndGet(durationMs)

        if (!success) {
            errors.incrementAndGet()
        }

        // Update max/min
        synchronized(this) {
            if (durationMs > maxDuration) {
                maxDuration = durationMs
            }
            if (durationMs < minDuration) {
                minDuration = durationMs
            }
        }
    }
}

/**
 * Slow query information.
 */
data class SlowQuery(
    val sql: String,
    val parameters: Map<String, Any?>,
    val durationMs: Long,
    val timestamp: Long
)
