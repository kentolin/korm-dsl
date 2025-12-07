// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/Main.kt

package com.korm.examples.enterprise

import com.korm.dsl.cache.CacheConfig
import com.korm.dsl.cache.EvictionStrategyType
import com.korm.dsl.cache.InMemoryCache
import com.korm.dsl.core.Database
import com.korm.dsl.migrations.MigrationEngine
import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.HealthCheckRegistry
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.dsl.monitoring.alerts.AlertSystem
import com.korm.dsl.monitoring.prometheus.PrometheusExporter
import com.korm.examples.enterprise.api.startServer
import com.korm.examples.enterprise.config.AppConfig
import com.korm.examples.enterprise.migrations.runMigrations
import com.korm.examples.enterprise.monitoring.setupMonitoring
import com.korm.examples.enterprise.services.*
import org.slf4j.LoggerFactory
import java.time.Duration

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    logger.info("Starting KORM Enterprise Application")

    try {
        // Load configuration
        val config = AppConfig.load()
        logger.info("Configuration loaded")

        // Initialize database
        val database = Database.connect(
            url = config.database.url,
            driver = config.database.driver,
            user = config.database.user,
            password = config.database.password,
            poolSize = config.database.poolSize,
            connectionTimeout = config.database.connectionTimeout,
            maxLifetime = config.database.maxLifetime
        )
        logger.info("Database connected: ${config.database.url}")

        // Run migrations
        logger.info("Running database migrations...")
        runMigrations(database)
        logger.info("Migrations completed")

        // Initialize monitoring
        val metrics = DatabaseMetrics()
        val queryMonitor = QueryMonitor(slowQueryThresholdMs = config.monitoring.slowQueryThresholdMs)
        val healthRegistry = HealthCheckRegistry()
        val alertSystem = AlertSystem()
        val prometheusExporter = PrometheusExporter()

        setupMonitoring(database, metrics, queryMonitor, healthRegistry, alertSystem)
        logger.info("Monitoring initialized")

        // Initialize cache
        val cache = InMemoryCache<String, Any>(
            config = CacheConfig(
                maxSize = config.cache.maxSize,
                defaultTTL = Duration.ofMinutes(config.cache.ttlMinutes),
                evictionStrategy = EvictionStrategyType.LRU,
                recordStats = true
            )
        )
        logger.info("Cache initialized with max size: ${config.cache.maxSize}")

        // Initialize services
        val userService = UserService(database, cache, metrics, queryMonitor)
        val productService = ProductService(database, cache, metrics, queryMonitor)
        val orderService = OrderService(database, productService, metrics, queryMonitor)
        val reportingService = ReportingService(database, queryMonitor)
        val auditService = AuditService(database)

        logger.info("Services initialized")

        // Start HTTP server
        val server = startServer(
            config = config,
            database = database,
            userService = userService,
            productService = productService,
            orderService = orderService,
            reportingService = reportingService,
            auditService = auditService,
            healthRegistry = healthRegistry,
            metrics = metrics,
            queryMonitor = queryMonitor,
            cache = cache,
            prometheusExporter = prometheusExporter
        )

        logger.info("Enterprise application started successfully")
        logger.info("HTTP Server: http://localhost:${config.server.port}")
        logger.info("Health: http://localhost:${config.server.port}/health")
        logger.info("Metrics: http://localhost:${config.server.port}/metrics")
        logger.info("Admin: http://localhost:${config.server.port}/admin")

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("Shutting down application...")
            server.stop(1000, 2000)
            database.close()
            logger.info("Application shutdown complete")
        })

    } catch (e: Exception) {
        logger.error("Failed to start application", e)
        System.exit(1)
    }
}
