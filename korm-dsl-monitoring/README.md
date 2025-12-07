# KORM DSL - Monitoring Module

Comprehensive monitoring and metrics collection for KORM DSL.

## Features

- **Metrics Collection**: Counters, Gauges, Histograms, Timers, Summaries
- **Database Metrics**: Connection pool, queries, transactions
- **Query Monitoring**: Slow query detection, query statistics
- **Health Checks**: Database, connection pool, composite checks
- **Prometheus Integration**: Export metrics in Prometheus format
- **Micrometer Integration**: Bridge to Micrometer metrics
- **JMX Integration**: Expose metrics via JMX MBeans
- **Alert System**: Configurable alerts with multiple handlers

## Usage

### Basic Metrics
```kotlin
// Counter
val requestCounter = GlobalMetricsRegistry.counter("http.requests.total")
requestCounter.increment()

// Gauge
val activeUsers = GlobalMetricsRegistry.gauge("users.active")
activeUsers.set(42.0)

// Timer
val queryTimer = GlobalMetricsRegistry.timer("db.query.duration")
queryTimer.record {
    database.select(Users) { where(Users.age gt 18) }
}

// Histogram
val responseSize = GlobalMetricsRegistry.histogram("http.response.size")
responseSize.record(1024)
```

### Database Metrics
```kotlin
val dbMetrics = DatabaseMetrics()

// Record connection pool stats
dbMetrics.recordConnectionPoolStats(
    active = 5,
    idle = 3,
    total = 10,
    waiting = 0
)

// Record query execution
dbMetrics.recordQuery(durationNanos = 1_000_000)

// Get summary
val summary = dbMetrics.getSummary()
println("Total queries: ${summary.totalQueries}")
println("Average query time: ${summary.averageQueryTime}ms")
```

### Query Monitoring
```kotlin
val queryMonitor = QueryMonitor(slowQueryThresholdMs = 1000)

queryMonitor.recordQuery(
    sql = "SELECT * FROM users WHERE age > ?",
    parameters = mapOf("age" to 18),
    durationMs = 1500
)

// Get slow queries
val slowQueries = queryMonitor.getSlowQueries(limit = 10)
slowQueries.forEach { query ->
    println("Slow query (${query.durationMs}ms): ${query.sql}")
}

// Get statistics
val stats = queryMonitor.getSlowestQueries(10)
stats.forEach { stat ->
    println("${stat.sql}: avg=${stat.averageDuration}ms, max=${stat.maxDuration}ms")
}
```

### Health Checks
```kotlin
val healthRegistry = HealthCheckRegistry()

// Database health check
healthRegistry.register(DatabaseHealthCheck(database))

// Custom health check
healthRegistry.register(object : HealthCheck {
    override val name = "api"
    override fun check(): HealthCheckResult {
        return try {
            // Check API availability
            HealthCheckResult(HealthStatus.UP)
        } catch (e: Exception) {
            HealthCheckResult(HealthStatus.DOWN, error = e.message)
        }
    }
})

// Run all checks
val results = healthRegistry.runChecks()
val overall = healthRegistry.getOverallStatus()

if (overall.isHealthy()) {
    println("System is healthy")
} else {
    println("System is unhealthy: ${overall.status}")
}
```

### Prometheus Export
```kotlin
val exporter = PrometheusExporter()

// Export metrics
val prometheusMetrics = exporter.export()

// Expose via HTTP endpoint
get("/metrics") {
    call.respondText(prometheusMetrics, ContentType.Text.Plain)
}
```

### Alert System
```kotlin
val alertSystem = AlertSystem()

// Add alert condition
alertSystem.addCondition(
    ThresholdCondition(
        name = "high_error_rate",
        severity = AlertSeverity.WARNING,
        threshold = 5.0,
        valueSupplier = { errorRate },
        comparison = { value, threshold -> value > threshold },
        messageTemplate = { value, threshold ->
            "Error rate ($value%) exceeds threshold ($threshold%)"
        }
    )
)

// Check conditions periodically
scheduler.scheduleAtFixedRate(
    initialDelay = 0,
    period = 60,
    unit = TimeUnit.SECONDS
) {
    alertSystem.checkConditions()
}
```

## See Also

- [Monitoring Guide](../../docs/advanced/monitoring.md)
- [Examples](../../examples/)
