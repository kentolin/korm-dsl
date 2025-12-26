package com.korm.dsl.monitoring

import java.time.Instant

/**
 * Monitor for tracking query execution
 * Instruments queries and publishes events
 */
class QueryMonitor(
    private val eventBus: EventBus = EventBus.global
) {

    /**
     * Execute a query with monitoring
     */
    fun <T> monitor(
        sql: String,
        parameters: List<Any?> = emptyList(),
        queryType: QueryType = inferQueryType(sql),
        tableName: String? = extractTableName(sql),
        block: () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        var success = true
        var exception: Throwable? = null
        var result: T? = null

        try {
            result = block()
            return result
        } catch (e: Throwable) {
            success = false
            exception = e
            throw e
        } finally {
            val executionTime = System.currentTimeMillis() - startTime

            // Determine rows affected
            val rowsAffected = when (result) {
                is Int -> result
                is List<*> -> result.size
                else -> null
            }

            // Publish query event
            val event = QueryEvent(
                sql = sql,
                parameters = parameters,
                executionTimeMs = executionTime,
                rowsAffected = rowsAffected,
                success = success,
                exception = exception,
                timestamp = Instant.now(),
                queryType = queryType,
                tableName = tableName
            )

            eventBus.publish(event)
        }
    }

    /**
     * Monitor a transaction
     */
    fun <T> monitorTransaction(block: () -> T): T {
        val startTime = System.currentTimeMillis()

        // Publish BEGIN event
        eventBus.publish(
            TransactionEvent(
                action = TransactionAction.BEGIN,
                timestamp = Instant.now()
            )
        )

        var success = true
        var exception: Throwable? = null

        try {
            val result = block()

            // Publish COMMIT event
            val duration = System.currentTimeMillis() - startTime
            eventBus.publish(
                TransactionEvent(
                    action = TransactionAction.COMMIT,
                    durationMs = duration,
                    success = true,
                    timestamp = Instant.now()
                )
            )

            return result
        } catch (e: Throwable) {
            success = false
            exception = e

            // Publish ROLLBACK event
            val duration = System.currentTimeMillis() - startTime
            eventBus.publish(
                TransactionEvent(
                    action = TransactionAction.ROLLBACK,
                    durationMs = duration,
                    success = false,
                    exception = e,
                    timestamp = Instant.now()
                )
            )

            throw e
        }
    }

    /**
     * Monitor a batch operation
     */
    fun <T> monitorBatch(
        operation: String,
        batchSize: Int,
        totalItems: Int,
        block: () -> T
    ): T {
        val startTime = System.currentTimeMillis()
        var success = true
        var exception: Throwable? = null

        try {
            val result = block()
            return result
        } catch (e: Throwable) {
            success = false
            exception = e
            throw e
        } finally {
            val executionTime = System.currentTimeMillis() - startTime

            val event = BatchEvent(
                operation = operation,
                batchSize = batchSize,
                totalItems = totalItems,
                executionTimeMs = executionTime,
                success = success,
                exception = exception,
                timestamp = Instant.now()
            )

            eventBus.publish(event)
        }
    }

    /**
     * Publish a connection event
     */
    fun publishConnectionEvent(
        eventName: String,
        activeConnections: Int,
        idleConnections: Int,
        totalConnections: Int,
        pendingThreads: Int = 0
    ) {
        val event = ConnectionEvent(
            eventName = eventName,
            activeConnections = activeConnections,
            idleConnections = idleConnections,
            totalConnections = totalConnections,
            pendingThreads = pendingThreads,
            timestamp = Instant.now()
        )

        eventBus.publish(event)
    }

    companion object {
        /**
         * Infer query type from SQL
         */
        fun inferQueryType(sql: String): QueryType {
            val normalized = sql.trim().uppercase()
            return when {
                normalized.startsWith("SELECT") -> QueryType.SELECT
                normalized.startsWith("INSERT") -> QueryType.INSERT
                normalized.startsWith("UPDATE") -> QueryType.UPDATE
                normalized.startsWith("DELETE") -> QueryType.DELETE
                normalized.startsWith("CREATE") ||
                    normalized.startsWith("ALTER") ||
                    normalized.startsWith("DROP") -> QueryType.DDL
                normalized.startsWith("BEGIN") ||
                    normalized.startsWith("COMMIT") ||
                    normalized.startsWith("ROLLBACK") -> QueryType.TRANSACTION
                else -> QueryType.UNKNOWN
            }
        }

        /**
         * Extract table name from SQL
         */
        fun extractTableName(sql: String): String? {
            val normalized = sql.trim().uppercase()

            // Simple regex-based extraction
            val patterns = listOf(
                Regex("FROM\\s+([a-zA-Z_][a-zA-Z0-9_]*)"),
                Regex("INTO\\s+([a-zA-Z_][a-zA-Z0-9_]*)"),
                Regex("UPDATE\\s+([a-zA-Z_][a-zA-Z0-9_]*)"),
                Regex("TABLE\\s+(?:IF\\s+(?:NOT\\s+)?EXISTS\\s+)?([a-zA-Z_][a-zA-Z0-9_]*)"),
                Regex("DELETE\\s+FROM\\s+([a-zA-Z_][a-zA-Z0-9_]*)")
            )

            for (pattern in patterns) {
                val match = pattern.find(normalized)
                if (match != null) {
                    return match.groupValues[1].lowercase()
                }
            }

            return null
        }
    }
}
