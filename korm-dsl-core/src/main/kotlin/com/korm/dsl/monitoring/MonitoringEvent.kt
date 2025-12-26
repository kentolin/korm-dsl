package com.korm.dsl.monitoring

import java.time.Instant

/**
 * Base class for all monitoring events
 */
sealed class MonitoringEvent {
    abstract val timestamp: Instant
    abstract val eventType: String
}

/**
 * Query execution event
 */
data class QueryEvent(
    val sql: String,
    val parameters: List<Any?> = emptyList(),
    val executionTimeMs: Long,
    val rowsAffected: Int? = null,
    val success: Boolean = true,
    val exception: Throwable? = null,
    override val timestamp: Instant = Instant.now(),
    val queryType: QueryType = QueryType.UNKNOWN,
    val tableName: String? = null
) : MonitoringEvent() {
    override val eventType: String = "QUERY"

    val isSlowQuery: Boolean
        get() = executionTimeMs > 1000 // Default: queries over 1 second

    override fun toString(): String {
        return "QueryEvent(type=$queryType, table=$tableName, time=${executionTimeMs}ms, rows=$rowsAffected, success=$success)"
    }
}

/**
 * Type of SQL query
 */
enum class QueryType {
    SELECT,
    INSERT,
    UPDATE,
    DELETE,
    DDL,
    TRANSACTION,
    UNKNOWN
}

/**
 * Connection pool event
 */
data class ConnectionEvent(
    val eventName: String,
    val activeConnections: Int,
    val idleConnections: Int,
    val totalConnections: Int,
    val pendingThreads: Int = 0,
    override val timestamp: Instant = Instant.now()
) : MonitoringEvent() {
    override val eventType: String = "CONNECTION"

    override fun toString(): String {
        return "ConnectionEvent(event=$eventName, active=$activeConnections, idle=$idleConnections, total=$totalConnections, pending=$pendingThreads)"
    }
}

/**
 * Transaction event
 */
data class TransactionEvent(
    val action: TransactionAction,
    val durationMs: Long? = null,
    val success: Boolean = true,
    val exception: Throwable? = null,
    override val timestamp: Instant = Instant.now()
) : MonitoringEvent() {
    override val eventType: String = "TRANSACTION"

    override fun toString(): String {
        return "TransactionEvent(action=$action, duration=$durationMs ms, success=$success)"
    }
}

/**
 * Transaction action type
 */
enum class TransactionAction {
    BEGIN,
    COMMIT,
    ROLLBACK
}

/**
 * Migration event
 */
data class MigrationEvent(
    val version: Long,
    val description: String,
    val action: MigrationAction,
    val executionTimeMs: Long,
    val success: Boolean = true,
    val exception: Throwable? = null,
    override val timestamp: Instant = Instant.now()
) : MonitoringEvent() {
    override val eventType: String = "MIGRATION"

    override fun toString(): String {
        return "MigrationEvent(version=$version, action=$action, time=${executionTimeMs}ms, success=$success)"
    }
}

/**
 * Migration action type
 */
enum class MigrationAction {
    UP,
    DOWN
}

/**
 * Batch operation event
 */
data class BatchEvent(
    val operation: String,
    val batchSize: Int,
    val totalItems: Int,
    val executionTimeMs: Long,
    val success: Boolean = true,
    val exception: Throwable? = null,
    override val timestamp: Instant = Instant.now()
) : MonitoringEvent() {
    override val eventType: String = "BATCH"

    val itemsPerSecond: Double
        get() = if (executionTimeMs > 0) (totalItems * 1000.0) / executionTimeMs else 0.0

    override fun toString(): String {
        return "BatchEvent(operation=$operation, items=$totalItems, batchSize=$batchSize, time=${executionTimeMs}ms, rate=${String.format("%.2f", itemsPerSecond)} items/s)"
    }
}
