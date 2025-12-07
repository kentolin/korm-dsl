// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/monitoring/MonitoringSetup.kt

package com.korm.examples.enterprise.monitoring

import com.korm.dsl.core.Database
import com.korm.dsl.monitoring.*
import com.korm.dsl.monitoring.alerts.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun setupMonitoring(
    database: Database,
    metrics: DatabaseMetrics,
    queryMonitor: QueryMonitor,
    healthRegistry: HealthCheckRegistry,
    alertSystem: AlertSystem
) {
    // Register health checks
    healthRegistry.register(DatabaseHealthCheck(database))
    healthRegistry.register(
        ConnectionPoolHealthCheck(
            getPoolStats = {
                val summary = metrics.getSummary()
                mapOf(
                    "active" to summary.activeConnections,
                    "total" to summary.totalConnections
                )
            }
        )
    )

    // Setup alerts
    alertSystem.addCondition(
        ThresholdCondition(
            name = "high_query_error_rate",
            severity = AlertSeverity.ERROR,
            threshold = 5.0,
            valueSupplier = {
                val summary = metrics.getSummary()
                if (summary.totalQueries > 0) {
                    (summary.queryErrors.toDouble() / summary.totalQueries) * 100
                } else {
                    0.0
                }
            },
            comparison = { value, threshold -> value > threshold },
            messageTemplate = { value, threshold ->
                "Query error rate (${"%.2f".format(value)}%) exceeds threshold ($threshold%)"
            }
        )
    )

    alertSystem.addCondition(
        ThresholdCondition(
            name = "high_connection_usage",
            severity = AlertSeverity.WARNING,
            threshold = 80.0,
            valueSupplier = {
                val summary = metrics.getSummary()
                if (summary.totalConnections > 0) {
                    (summary.activeConnections.toDouble() / summary.totalConnections) * 100
                } else {
                    0.0
                }
            },
            comparison = { value, threshold -> value > threshold },
            messageTemplate = { value, threshold ->
                "Connection pool usage (${"%.2f".format(value)}%) exceeds threshold ($threshold%)"
            }
        )
    )

    // Schedule periodic checks
    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate(
        {
            try {
                alertSystem.checkConditions()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        },
        0,
        60,
        TimeUnit.SECONDS
    )
}ye
