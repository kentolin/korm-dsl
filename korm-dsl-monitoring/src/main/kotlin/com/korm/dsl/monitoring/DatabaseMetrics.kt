// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/DatabaseMetrics.kt

package com.korm.dsl.monitoring

/**
 * Database-specific metrics collector.
 */
class DatabaseMetrics(
    private val registry: MetricsRegistry = GlobalMetricsRegistry.registry
) {
    // Connection pool metrics
    private val activeConnections = registry.gauge("db.connections.active")
    private val idleConnections = registry.gauge("db.connections.idle")
    private val totalConnections = registry.gauge("db.connections.total")
    private val waitingThreads = registry.gauge("db.connections.waiting")

    // Query metrics
    private val queryCount = registry.counter("db.queries.total")
    private val queryErrors = registry.counter("db.queries.errors")
    private val queryTimer = registry.timer("db.queries.duration")

    // Transaction metrics
    private val transactionCount = registry.counter("db.transactions.total")
    private val transactionCommits = registry.counter("db.transactions.commits")
    private val transactionRollbacks = registry.counter("db.transactions.rollbacks")
    private val transactionTimer = registry.timer("db.transactions.duration")

    // Operation metrics
    private val selectCount = registry.counter("db.operations.select")
    private val insertCount = registry.counter("db.operations.insert")
    private val updateCount = registry.counter("db.operations.update")
    private val deleteCount = registry.counter("db.operations.delete")

    /**
     * Record connection pool stats.
     */
    fun recordConnectionPoolStats(
        active: Int,
        idle: Int,
        total: Int,
        waiting: Int
    ) {
        activeConnections.set(active.toDouble())
        idleConnections.set(idle.toDouble())
        totalConnections.set(total.toDouble())
        waitingThreads.set(waiting.toDouble())
    }

    /**
     * Record query execution.
     */
    fun recordQuery(durationNanos: Long, error: Throwable? = null) {
        queryCount.increment()
        queryTimer.record(durationNanos)

        if (error != null) {
            queryErrors.increment()
        }
    }

    /**
     * Record transaction.
     */
    fun recordTransaction(
        durationNanos: Long,
        committed: Boolean
    ) {
        transactionCount.increment()
        transactionTimer.record(durationNanos)

        if (committed) {
            transactionCommits.increment()
        } else {
            transactionRollbacks.increment()
        }
    }

    /**
     * Record SELECT operation.
     */
    fun recordSelect() {
        selectCount.increment()
    }

    /**
     * Record INSERT operation.
     */
    fun recordInsert() {
        insertCount.increment()
    }

    /**
     * Record UPDATE operation.
     */
    fun recordUpdate() {
        updateCount.increment()
    }

    /**
     * Record DELETE operation.
     */
    fun recordDelete() {
        deleteCount.increment()
    }

    /**
     * Get summary of all metrics.
     */
    fun getSummary(): DatabaseMetricsSummary {
        return DatabaseMetricsSummary(
            activeConnections = activeConnections.value().toInt(),
            idleConnections = idleConnections.value().toInt(),
            totalConnections = totalConnections.value().toInt(),
            waitingThreads = waitingThreads.value().toInt(),
            totalQueries = queryCount.count(),
            queryErrors = queryErrors.count(),
            averageQueryTime = queryTimer.mean(),
            maxQueryTime = queryTimer.max(),
            totalTransactions = transactionCount.count(),
            commits = transactionCommits.count(),
            rollbacks = transactionRollbacks.count(),
            averageTransactionTime = transactionTimer.mean(),
            selectCount = selectCount.count(),
            insertCount = insertCount.count(),
            updateCount = updateCount.count(),
            deleteCount = deleteCount.count()
        )
    }
}

/**
 * Database metrics summary.
 */
data class DatabaseMetricsSummary(
    val activeConnections: Int,
    val idleConnections: Int,
    val totalConnections: Int,
    val waitingThreads: Int,
    val totalQueries: Long,
    val queryErrors: Long,
    val averageQueryTime: Double,
    val maxQueryTime: Long,
    val totalTransactions: Long,
    val commits: Long,
    val rollbacks: Long,
    val averageTransactionTime: Double,
    val selectCount: Long,
    val insertCount: Long,
    val updateCount: Long,
    val deleteCount: Long
)
