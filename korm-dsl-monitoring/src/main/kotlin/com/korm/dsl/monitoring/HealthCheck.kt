// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/HealthCheck.kt

package com.korm.dsl.monitoring

import com.korm.dsl.core.Database

/**
 * Health check status.
 */
enum class HealthStatus {
    UP,
    DOWN,
    DEGRADED,
    UNKNOWN
}

/**
 * Health check result.
 */
data class HealthCheckResult(
    val status: HealthStatus,
    val details: Map<String, Any> = emptyMap(),
    val error: String? = null
) {
    fun isHealthy(): Boolean = status == HealthStatus.UP
}

/**
 * Health check interface.
 */
interface HealthCheck {
    /**
     * Get health check name.
     */
    val name: String

    /**
     * Execute health check.
     */
    fun check(): HealthCheckResult
}

/**
 * Database health check.
 */
class DatabaseHealthCheck(
    private val database: Database,
    override val name: String = "database"
) : HealthCheck {

    override fun check(): HealthCheckResult {
        return try {
            // Try to execute a simple query
            database.transaction {
                execute("SELECT 1")
            }

            HealthCheckResult(
                status = HealthStatus.UP,
                details = mapOf(
                    "database" to "connected"
                )
            )
        } catch (e: Exception) {
            HealthCheckResult(
                status = HealthStatus.DOWN,
                error = e.message
            )
        }
    }
}

/**
 * Connection pool health check.
 */
class ConnectionPoolHealthCheck(
    private val getPoolStats: () -> Map<String, Int>,
    override val name: String = "connection_pool"
) : HealthCheck {

    override fun check(): HealthCheckResult {
        return try {
            val stats = getPoolStats()
            val active = stats["active"] ?: 0
            val total = stats["total"] ?: 0
            val utilization = if (total > 0) (active.toDouble() / total) * 100 else 0.0

            val status = when {
                utilization > 90 -> HealthStatus.DEGRADED
                utilization > 50 -> HealthStatus.UP
                else -> HealthStatus.UP
            }

            HealthCheckResult(
                status = status,
                details = mapOf(
                    "active_connections" to active,
                    "total_connections" to total,
                    "utilization_percent" to utilization
                )
            )
        } catch (e: Exception) {
            HealthCheckResult(
                status = HealthStatus.DOWN,
                error = e.message
            )
        }
    }
}

/**
 * Composite health check.
 */
class CompositeHealthCheck(
    private val checks: List<HealthCheck>,
    override val name: String = "application"
) : HealthCheck {

    override fun check(): HealthCheckResult {
        val results = checks.associateBy({ it.name }, { it.check() })

        val overallStatus = when {
            results.values.any { it.status == HealthStatus.DOWN } -> HealthStatus.DOWN
            results.values.any { it.status == HealthStatus.DEGRADED } -> HealthStatus.DEGRADED
            results.values.all { it.status == HealthStatus.UP } -> HealthStatus.UP
            else -> HealthStatus.UNKNOWN
        }

        return HealthCheckResult(
            status = overallStatus,
            details = results.mapValues { (_, result) ->
                mapOf(
                    "status" to result.status.name,
                    "details" to result.details,
                    "error" to result.error
                )
            }
        )
    }
}

/**
 * Health check registry.
 */
class HealthCheckRegistry {
    private val checks = mutableMapOf<String, HealthCheck>()

    /**
     * Register a health check.
     */
    fun register(check: HealthCheck) {
        checks[check.name] = check
    }

    /**
     * Unregister a health check.
     */
    fun unregister(name: String) {
        checks.remove(name)
    }

    /**
     * Run all health checks.
     */
    fun runChecks(): Map<String, HealthCheckResult> {
        return checks.mapValues { (_, check) -> check.check() }
    }

    /**
     * Run a specific health check.
     */
    fun runCheck(name: String): HealthCheckResult? {
        return checks[name]?.check()
    }

    /**
     * Get overall health status.
     */
    fun getOverallStatus(): HealthCheckResult {
        val composite = CompositeHealthCheck(checks.values.toList())
        return composite.check()
    }
}
