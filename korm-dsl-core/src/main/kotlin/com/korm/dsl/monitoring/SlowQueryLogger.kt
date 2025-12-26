package com.korm.dsl.monitoring

import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter

/**
 * Logger for slow queries
 * Identifies and logs queries that exceed a threshold
 */
class SlowQueryLogger(
    /**
     * Threshold in milliseconds - queries slower than this will be logged
     */
    private val thresholdMs: Long = 1000,

    /**
     * Whether to log stack traces
     */
    private val logStackTrace: Boolean = false
) : QueryEventListener() {

    private val logger = LoggerFactory.getLogger(SlowQueryLogger::class.java)
    private val dateFormatter = DateTimeFormatter.ISO_INSTANT

    private var slowQueryCount = 0L
    private var totalSlowQueryTime = 0L

    override fun handleTypedEvent(event: QueryEvent) {
        if (event.executionTimeMs >= thresholdMs) {
            slowQueryCount++
            totalSlowQueryTime += event.executionTimeMs

            logSlowQuery(event)
        }
    }

    private fun logSlowQuery(event: QueryEvent) {
        val message = buildString {
            appendLine("⚠️ SLOW QUERY DETECTED")
            appendLine("  Time: ${dateFormatter.format(event.timestamp)}")
            appendLine("  Execution: ${event.executionTimeMs}ms (threshold: ${thresholdMs}ms)")
            appendLine("  Type: ${event.queryType}")
            event.tableName?.let { appendLine("  Table: $it") }
            event.rowsAffected?.let { appendLine("  Rows: $it") }
            appendLine("  SQL: ${event.sql}")
            if (event.parameters.isNotEmpty()) {
                appendLine("  Parameters: ${event.parameters}")
            }
            if (!event.success && event.exception != null) {
                appendLine("  Error: ${event.exception.message}")
            }
        }

        logger.warn(message)

        if (logStackTrace && event.exception != null) {
            logger.warn("Stack trace:", event.exception)
        }
    }

    /**
     * Get statistics about slow queries
     */
    fun getStats(): SlowQueryStats {
        return SlowQueryStats(
            count = slowQueryCount,
            totalTimeMs = totalSlowQueryTime,
            averageTimeMs = if (slowQueryCount > 0) totalSlowQueryTime / slowQueryCount else 0,
            thresholdMs = thresholdMs
        )
    }

    /**
     * Reset statistics
     */
    fun resetStats() {
        slowQueryCount = 0
        totalSlowQueryTime = 0
    }
}

/**
 * Statistics about slow queries
 */
data class SlowQueryStats(
    val count: Long,
    val totalTimeMs: Long,
    val averageTimeMs: Long,
    val thresholdMs: Long
) {
    override fun toString(): String {
        return "SlowQueryStats(count=$count, totalTime=${totalTimeMs}ms, avgTime=${averageTimeMs}ms, threshold=${thresholdMs}ms)"
    }
}
