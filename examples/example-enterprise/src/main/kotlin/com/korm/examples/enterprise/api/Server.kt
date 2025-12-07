// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/Server.kt

package com.korm.examples.enterprise.api

import com.korm.dsl.cache.Cache
import com.korm.dsl.core.Database
import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.HealthCheckRegistry
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.dsl.monitoring.prometheus.PrometheusExporter
import com.korm.examples.enterprise.config.AppConfig
import com.korm.examples.enterprise.services.*
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

fun startServer(
    config: AppConfig,
    database: Database,
    userService: UserService,
    productService: ProductService,
    orderService: OrderService,
    reportingService: ReportingService,
    auditService: AuditService,
    healthRegistry: HealthCheckRegistry,
    metrics: DatabaseMetrics,
    queryMonitor: QueryMonitor,
    cache: Cache<String, Any>,
    prometheusExporter: PrometheusExporter
): NettyApplicationEngine {

    return embeddedServer(Netty, port = config.server.port, host = config.server.host) {
        module(
            userService,
            productService,
            orderService,
            reportingService,
            auditService,
            healthRegistry,
            metrics,
            queryMonitor,
            cache,
            prometheusExporter
        )
    }.start(wait = false)
}

fun Application.module(
    userService: UserService,
    productService: ProductService,
    orderService: OrderService,
    reportingService: ReportingService,
    auditService: AuditService,
    healthRegistry: HealthCheckRegistry,
    metrics: DatabaseMetrics,
    queryMonitor: QueryMonitor,
    cache: Cache<String, Any>,
    prometheusExporter: PrometheusExporter
) {
    // Install plugins
    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to (cause.message ?: "Bad request"))
            )
        }
    }

    // Configure routing
    routing {
        userRoutes(userService, auditService)
        productRoutes(productService, auditService)
        orderRoutes(orderService, auditService)
        reportRoutes(reportingService)
        healthRoutes(healthRegistry)
        metricsRoutes(metrics, queryMonitor, prometheusExporter)
        adminRoutes(cache, queryMonitor)
    }
}
