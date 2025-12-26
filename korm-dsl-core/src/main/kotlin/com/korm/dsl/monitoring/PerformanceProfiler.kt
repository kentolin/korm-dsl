package com.korm.dsl.monitoring

import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Performance profiler that collects detailed metrics
 */
class PerformanceProfiler : EventListener {

    private val queryStats = ConcurrentHashMap<QueryType, QueryTypeStats>()
    private val tableStats = ConcurrentHashMap<String, TableStats>()
    private val connectionStats = ConnectionStats()
    private val transactionStats = TransactionStats()
    private val batchStats = BatchStats()

    private var startTime: Instant = Instant.now()

    override fun onEvent(event: MonitoringEvent) {
        when (event) {
            is QueryEvent -> recordQuery(event)
            is ConnectionEvent -> recordConnection(event)
            is TransactionEvent -> recordTransaction(event)
            is BatchEvent -> recordBatch(event)
            is MigrationEvent -> {} // Migrations handled separately
        }
    }

    private fun recordQuery(event: QueryEvent) {
        // Update query type stats
        val typeStats = queryStats.computeIfAbsent(event.queryType) { QueryTypeStats(it) }
        typeStats.record(event)

        // Update table stats
        event.tableName?.let { tableName ->
            val tableStatsObj = tableStats.computeIfAbsent(tableName) { TableStats(it) }
            tableStatsObj.record(event)
        }
    }

    private fun recordConnection(event: ConnectionEvent) {
        connectionStats.record(event)
    }

    private fun recordTransaction(event: TransactionEvent) {
        transactionStats.record(event)
    }

    private fun recordBatch(event: BatchEvent) {
        batchStats.record(event)
    }

    /**
     * Get complete performance report
     */
    fun getReport(): PerformanceReport {
        val duration = Duration.between(startTime, Instant.now())

        return PerformanceReport(
            duration = duration,
            queryStats = queryStats.values.map { it.snapshot() },
            tableStats = tableStats.values.map { it.snapshot() },
            connectionStats = connectionStats.snapshot(),
            transactionStats = transactionStats.snapshot(),
            batchStats = batchStats.snapshot()
        )
    }

    /**
     * Print formatted report
     */
    fun printReport() {
        val report = getReport()
        println(report.format())
    }

    /**
     * Reset all statistics
     */
    fun reset() {
        queryStats.clear()
        tableStats.clear()
        connectionStats.reset()
        transactionStats.reset()
        batchStats.reset()
        startTime = Instant.now()
    }
}

/**
 * Statistics for a specific query type
 */
class QueryTypeStats(val type: QueryType) {
    private val count = AtomicLong(0)
    private val totalTime = AtomicLong(0)
    private val errorCount = AtomicLong(0)
    private var minTime = Long.MAX_VALUE
    private var maxTime = Long.MIN_VALUE

    fun record(event: QueryEvent) {
        count.incrementAndGet()
        totalTime.addAndGet(event.executionTimeMs)

        synchronized(this) {
            if (event.executionTimeMs < minTime) minTime = event.executionTimeMs
            if (event.executionTimeMs > maxTime) maxTime = event.executionTimeMs
        }

        if (!event.success) {
            errorCount.incrementAndGet()
        }
    }

    fun snapshot(): QueryTypeStatsSnapshot {
        val cnt = count.get()
        return QueryTypeStatsSnapshot(
            type = type,
            count = cnt,
            totalTimeMs = totalTime.get(),
            avgTimeMs = if (cnt > 0) totalTime.get() / cnt else 0,
            minTimeMs = if (minTime != Long.MAX_VALUE) minTime else 0,
            maxTimeMs = if (maxTime != Long.MIN_VALUE) maxTime else 0,
            errorCount = errorCount.get()
        )
    }
}

/**
 * Snapshot of query type statistics
 */
data class QueryTypeStatsSnapshot(
    val type: QueryType,
    val count: Long,
    val totalTimeMs: Long,
    val avgTimeMs: Long,
    val minTimeMs: Long,
    val maxTimeMs: Long,
    val errorCount: Long
)

/**
 * Statistics for a specific table
 */
class TableStats(val tableName: String) {
    private val selectCount = AtomicLong(0)
    private val insertCount = AtomicLong(0)
    private val updateCount = AtomicLong(0)
    private val deleteCount = AtomicLong(0)
    private val totalTime = AtomicLong(0)

    fun record(event: QueryEvent) {
        when (event.queryType) {
            QueryType.SELECT -> selectCount.incrementAndGet()
            QueryType.INSERT -> insertCount.incrementAndGet()
            QueryType.UPDATE -> updateCount.incrementAndGet()
            QueryType.DELETE -> deleteCount.incrementAndGet()
            else -> {}
        }
        totalTime.addAndGet(event.executionTimeMs)
    }

    fun snapshot(): TableStatsSnapshot {
        return TableStatsSnapshot(
            tableName = tableName,
            selectCount = selectCount.get(),
            insertCount = insertCount.get(),
            updateCount = updateCount.get(),
            deleteCount = deleteCount.get(),
            totalTimeMs = totalTime.get()
        )
    }
}

/**
 * Snapshot of table statistics
 */
data class TableStatsSnapshot(
    val tableName: String,
    val selectCount: Long,
    val insertCount: Long,
    val updateCount: Long,
    val deleteCount: Long,
    val totalTimeMs: Long
) {
    val totalOperations: Long
        get() = selectCount + insertCount + updateCount + deleteCount
}

/**
 * Connection pool statistics
 */
class ConnectionStats {
    private var peakActiveConnections = 0
    private var peakPendingThreads = 0
    private val acquisitionCount = AtomicLong(0)

    fun record(event: ConnectionEvent) {
        synchronized(this) {
            if (event.activeConnections > peakActiveConnections) {
                peakActiveConnections = event.activeConnections
            }
            if (event.pendingThreads > peakPendingThreads) {
                peakPendingThreads = event.pendingThreads
            }
        }

        if (event.eventName == "connection_acquired") {
            acquisitionCount.incrementAndGet()
        }
    }

    fun snapshot(): ConnectionStatsSnapshot {
        return ConnectionStatsSnapshot(
            peakActiveConnections = peakActiveConnections,
            peakPendingThreads = peakPendingThreads,
            totalAcquisitions = acquisitionCount.get()
        )
    }

    fun reset() {
        synchronized(this) {
            peakActiveConnections = 0
            peakPendingThreads = 0
        }
        acquisitionCount.set(0)
    }
}

/**
 * Snapshot of connection statistics
 */
data class ConnectionStatsSnapshot(
    val peakActiveConnections: Int,
    val peakPendingThreads: Int,
    val totalAcquisitions: Long
)

/**
 * Transaction statistics
 */
class TransactionStats {
    private val commitCount = AtomicLong(0)
    private val rollbackCount = AtomicLong(0)
    private val totalTime = AtomicLong(0)

    fun record(event: TransactionEvent) {
        when (event.action) {
            TransactionAction.COMMIT -> commitCount.incrementAndGet()
            TransactionAction.ROLLBACK -> rollbackCount.incrementAndGet()
            TransactionAction.BEGIN -> {}
        }

        event.durationMs?.let { totalTime.addAndGet(it) }
    }

    fun snapshot(): TransactionStatsSnapshot {
        val commits = commitCount.get()
        val rollbacks = rollbackCount.get()
        val total = commits + rollbacks

        return TransactionStatsSnapshot(
            commitCount = commits,
            rollbackCount = rollbacks,
            totalTimeMs = totalTime.get(),
            avgTimeMs = if (total > 0) totalTime.get() / total else 0
        )
    }

    fun reset() {
        commitCount.set(0)
        rollbackCount.set(0)
        totalTime.set(0)
    }
}

/**
 * Snapshot of transaction statistics
 */
data class TransactionStatsSnapshot(
    val commitCount: Long,
    val rollbackCount: Long,
    val totalTimeMs: Long,
    val avgTimeMs: Long
)

/**
 * Batch operation statistics
 */
class BatchStats {
    private val operationCount = AtomicLong(0)
    private val totalItems = AtomicLong(0)
    private val totalTime = AtomicLong(0)

    fun record(event: BatchEvent) {
        operationCount.incrementAndGet()
        totalItems.addAndGet(event.totalItems.toLong())
        totalTime.addAndGet(event.executionTimeMs)
    }

    fun snapshot(): BatchStatsSnapshot {
        val ops = operationCount.get()
        val items = totalItems.get()
        val time = totalTime.get()

        return BatchStatsSnapshot(
            operationCount = ops,
            totalItems = items,
            totalTimeMs = time,
            avgItemsPerOperation = if (ops > 0) items / ops else 0,
            itemsPerSecond = if (time > 0) (items * 1000.0) / time else 0.0
        )
    }

    fun reset() {
        operationCount.set(0)
        totalItems.set(0)
        totalTime.set(0)
    }
}

/**
 * Snapshot of batch statistics
 */
data class BatchStatsSnapshot(
    val operationCount: Long,
    val totalItems: Long,
    val totalTimeMs: Long,
    val avgItemsPerOperation: Long,
    val itemsPerSecond: Double
)

/**
 * Complete performance report
 */
data class PerformanceReport(
    val duration: Duration,
    val queryStats: List<QueryTypeStatsSnapshot>,
    val tableStats: List<TableStatsSnapshot>,
    val connectionStats: ConnectionStatsSnapshot,
    val transactionStats: TransactionStatsSnapshot,
    val batchStats: BatchStatsSnapshot
) {
    /**
     * Format report as string
     */
    fun format(): String = buildString {
        appendLine("\n╔════════════════════════════════════════════════════════════════╗")
        appendLine("║              KORM Performance Report                          ║")
        appendLine("╚════════════════════════════════════════════════════════════════╝")
        appendLine()
        appendLine("Duration: ${duration.toMinutes()}m ${duration.seconds % 60}s")
        appendLine()

        // Query Statistics
        if (queryStats.isNotEmpty()) {
            appendLine("─── Query Statistics ───────────────────────────────────────────")
            queryStats.sortedByDescending { it.count }.forEach { stat ->
                appendLine("  ${stat.type.name.padEnd(12)} │ Count: ${stat.count.toString().padEnd(8)} │ " +
                    "Avg: ${stat.avgTimeMs}ms │ Min: ${stat.minTimeMs}ms │ Max: ${stat.maxTimeMs}ms │ " +
                    "Errors: ${stat.errorCount}")
            }
            appendLine()
        }

        // Table Statistics
        if (tableStats.isNotEmpty()) {
            appendLine("─── Table Statistics ───────────────────────────────────────────")
            tableStats.sortedByDescending { it.totalOperations }.forEach { stat ->
                appendLine("  ${stat.tableName.padEnd(20)} │ " +
                    "SEL: ${stat.selectCount.toString().padEnd(6)} │ " +
                    "INS: ${stat.insertCount.toString().padEnd(6)} │ " +
                    "UPD: ${stat.updateCount.toString().padEnd(6)} │ " +
                    "DEL: ${stat.deleteCount.toString().padEnd(6)} │ " +
                    "Time: ${stat.totalTimeMs}ms")
            }
            appendLine()
        }

        // Connection Statistics
        appendLine("─── Connection Pool ────────────────────────────────────────────")
        appendLine("  Peak Active Connections: ${connectionStats.peakActiveConnections}")
        appendLine("  Peak Pending Threads: ${connectionStats.peakPendingThreads}")
        appendLine("  Total Acquisitions: ${connectionStats.totalAcquisitions}")
        appendLine()

        // Transaction Statistics
        appendLine("─── Transactions ───────────────────────────────────────────────")
        appendLine("  Commits: ${transactionStats.commitCount}")
        appendLine("  Rollbacks: ${transactionStats.rollbackCount}")
        appendLine("  Avg Duration: ${transactionStats.avgTimeMs}ms")
        appendLine()

        // Batch Statistics
        if (batchStats.operationCount > 0) {
            appendLine("─── Batch Operations ───────────────────────────────────────────")
            appendLine("  Operations: ${batchStats.operationCount}")
            appendLine("  Total Items: ${batchStats.totalItems}")
            appendLine("  Avg Items/Operation: ${batchStats.avgItemsPerOperation}")
            appendLine("  Throughput: ${String.format("%.2f", batchStats.itemsPerSecond)} items/s")
            appendLine()
        }

        appendLine("════════════════════════════════════════════════════════════════")
    }
}
