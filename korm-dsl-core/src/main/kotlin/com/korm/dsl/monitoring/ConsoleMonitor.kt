package com.korm.dsl.monitoring

import java.time.format.DateTimeFormatter

/**
 * Monitor that outputs events to console
 */
class ConsoleMonitor(
    /**
     * Whether to show all queries (false = only slow queries)
     */
    private val showAllQueries: Boolean = false,

    /**
     * Slow query threshold in milliseconds
     */
    private val slowQueryThreshold: Long = 1000,

    /**
     * Whether to show SQL parameters
     */
    private val showParameters: Boolean = true,

    /**
     * Whether to use colored output
     */
    private val useColors: Boolean = true
) : EventListener {

    private val dateFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

    override fun onEvent(event: MonitoringEvent) {
        when (event) {
            is QueryEvent -> printQuery(event)
            is ConnectionEvent -> printConnection(event)
            is TransactionEvent -> printTransaction(event)
            is BatchEvent -> printBatch(event)
            is MigrationEvent -> printMigration(event)
        }
    }

    private fun printQuery(event: QueryEvent) {
        // Skip if not showing all queries and this isn't slow
        if (!showAllQueries && event.executionTimeMs < slowQueryThreshold) {
            return
        }

        val time = dateFormatter.format(event.timestamp)
        val duration = "${event.executionTimeMs}ms"
        val icon = when {
            !event.success -> color("✗", Color.RED)
            event.executionTimeMs >= slowQueryThreshold -> color("⚠", Color.YELLOW)
            else -> color("✓", Color.GREEN)
        }

        val typeColor = when (event.queryType) {
            QueryType.SELECT -> Color.BLUE
            QueryType.INSERT -> Color.GREEN
            QueryType.UPDATE -> Color.YELLOW
            QueryType.DELETE -> Color.RED
            else -> Color.RESET
        }

        println("$icon [$time] ${color(event.queryType.name, typeColor)} " +
            "${event.tableName ?: "?"} - $duration")

        if (showAllQueries || event.executionTimeMs >= slowQueryThreshold) {
            println("  SQL: ${event.sql}")

            if (showParameters && event.parameters.isNotEmpty()) {
                println("  Params: ${event.parameters}")
            }

            if (!event.success && event.exception != null) {
                println("  ${color("Error: ${event.exception.message}", Color.RED)}")
            }
        }
    }

    private fun printConnection(event: ConnectionEvent) {
        val time = dateFormatter.format(event.timestamp)
        println("${color("⚙", Color.CYAN)} [$time] Connection: ${event.eventName} " +
            "(active: ${event.activeConnections}, idle: ${event.idleConnections})")
    }

    private fun printTransaction(event: TransactionEvent) {
        val time = dateFormatter.format(event.timestamp)
        val icon = when {
            !event.success -> color("✗", Color.RED)
            event.action == TransactionAction.ROLLBACK -> color("↩", Color.YELLOW)
            else -> color("✓", Color.GREEN)
        }

        val actionText = when (event.action) {
            TransactionAction.BEGIN -> "BEGIN"
            TransactionAction.COMMIT -> "COMMIT"
            TransactionAction.ROLLBACK -> "ROLLBACK"
        }

        val duration = event.durationMs?.let { " - ${it}ms" } ?: ""
        println("$icon [$time] Transaction: $actionText$duration")

        if (!event.success && event.exception != null) {
            println("  ${color("Error: ${event.exception.message}", Color.RED)}")
        }
    }

    private fun printBatch(event: BatchEvent) {
        val time = dateFormatter.format(event.timestamp)
        val icon = if (event.success) color("✓", Color.GREEN) else color("✗", Color.RED)

        println("$icon [$time] Batch: ${event.operation} " +
            "(${event.totalItems} items, batch=${event.batchSize}) - ${event.executionTimeMs}ms " +
            "(${String.format("%.2f", event.itemsPerSecond)} items/s)")

        if (!event.success && event.exception != null) {
            println("  ${color("Error: ${event.exception.message}", Color.RED)}")
        }
    }

    private fun printMigration(event: MigrationEvent) {
        val time = dateFormatter.format(event.timestamp)
        val icon = if (event.success) color("✓", Color.GREEN) else color("✗", Color.RED)
        val arrow = if (event.action == MigrationAction.UP) "→" else "←"

        println("$icon [$time] Migration $arrow v${event.version}: ${event.description} - ${event.executionTimeMs}ms")

        if (!event.success && event.exception != null) {
            println("  ${color("Error: ${event.exception.message}", Color.RED)}")
        }
    }

    private fun color(text: String, color: Color): String {
        return if (useColors) "${color.code}$text${Color.RESET.code}" else text
    }

    /**
     * ANSI color codes
     */
    private enum class Color(val code: String) {
        RESET("\u001B[0m"),
        RED("\u001B[31m"),
        GREEN("\u001B[32m"),
        YELLOW("\u001B[33m"),
        BLUE("\u001B[34m"),
        CYAN("\u001B[36m")
    }
}
