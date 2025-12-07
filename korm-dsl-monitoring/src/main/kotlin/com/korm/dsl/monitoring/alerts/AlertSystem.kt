// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/alerts/AlertSystem.kt

package com.korm.dsl.monitoring.alerts

import org.slf4j.LoggerFactory

/**
 * Alert severity levels.
 */
enum class AlertSeverity {
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

/**
 * Alert message.
 */
data class Alert(
    val name: String,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Alert handler interface.
 */
interface AlertHandler {
    fun handle(alert: Alert)
}

/**
 * Logging alert handler.
 */
class LoggingAlertHandler : AlertHandler {
    private val logger = LoggerFactory.getLogger("KORM.Alerts")

    override fun handle(alert: Alert) {
        when (alert.severity) {
            AlertSeverity.INFO -> logger.info("[${alert.name}] ${alert.message}")
            AlertSeverity.WARNING -> logger.warn("[${alert.name}] ${alert.message}")
            AlertSeverity.ERROR -> logger.error("[${alert.name}] ${alert.message}")
            AlertSeverity.CRITICAL -> logger.error("[CRITICAL][${alert.name}] ${alert.message}")
        }
    }
}

/**
 * Alert condition interface.
 */
interface AlertCondition {
    val name: String
    val severity: AlertSeverity
    fun check(): Boolean
    fun getMessage(): String
}

/**
 * Threshold-based alert condition.
 */
class ThresholdCondition(
    override val name: String,
    override val severity: AlertSeverity,
    private val threshold: Double,
    private val valueSupplier: () -> Double,
    private val comparison: (Double, Double) -> Boolean,
    private val messageTemplate: (Double, Double) -> String
) : AlertCondition {

    override fun check(): Boolean {
        val value = valueSupplier()
        return comparison(value, threshold)
    }

    override fun getMessage(): String {
        val value = valueSupplier()
        return messageTemplate(value, threshold)
    }
}

/**
 * Alert system.
 */
class AlertSystem {
    private val handlers = mutableListOf<AlertHandler>()
    private val conditions = mutableListOf<AlertCondition>()

    init {
        // Default logging handler
        handlers.add(LoggingAlertHandler())
    }

    /**
     * Add alert handler.
     */
    fun addHandler(handler: AlertHandler) {
        handlers.add(handler)
    }

    /**
     * Add alert condition.
     */
    fun addCondition(condition: AlertCondition) {
        conditions.add(condition)
    }

    /**
     * Check all conditions and trigger alerts.
     */
    fun checkConditions() {
        conditions.forEach { condition ->
            if (condition.check()) {
                val alert = Alert(
                    name = condition.name,
                    severity = condition.severity,
                    message = condition.getMessage()
                )
                triggerAlert(alert)
            }
        }
    }

    /**
     * Trigger an alert.
     */
    fun triggerAlert(alert: Alert) {
        handlers.forEach { handler ->
            try {
                handler.handle(alert)
            } catch (e: Exception) {
                // Log but don't fail
                LoggerFactory.getLogger(AlertSystem::class.java)
                    .error("Alert handler failed", e)
            }
        }
    }
}
