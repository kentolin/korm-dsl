// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/MetricsRoutes.kt

package com.korm.examples.enterprise.api

import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.dsl.monitoring.prometheus.PrometheusExporter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.metricsRoutes(
    metrics: DatabaseMetrics,
    queryMonitor: QueryMonitor,
    prometheusExporter: PrometheusExporter
) {
    route("/metrics") {
        // Prometheus format
        get {
            val prometheusMetrics = prometheusExporter.export()
            call.respondText(prometheusMetrics, ContentType.Text.Plain)
        }

        // Database metrics
        get("/database") {
            val summary = metrics.getSummary()
            call.respond(summary)
        }

        // Query statistics
        get("/queries") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val stats = queryMonitor.getAllStats().take(limit)
            call.respond(stats)
        }

        // Slow queries
        get("/queries/slow") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val slowQueries = queryMonitor.getSlowQueries(limit)
            call.respond(slowQueries)
        }

        // Top queries
        get("/queries/top") {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val topQueries = queryMonitor.getSlowestQueries(limit)
            call.respond(topQueries)
        }
    }
}
