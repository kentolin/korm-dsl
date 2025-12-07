// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/jmx/JmxExporter.kt

package com.korm.dsl.monitoring.jmx

import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.DatabaseMetricsSummary
import java.lang.management.ManagementFactory
import javax.management.ObjectName

/**
 * JMX MBean interface for database metrics.
 */
interface DatabaseMetricsMBean {
    fun getActiveConnections(): Int
    fun getIdleConnections(): Int
    fun getTotalConnections(): Int
    fun getTotalQueries(): Long
    fun getQueryErrors(): Long
    fun getAverageQueryTimeMs(): Double
    fun getTotalTransactions(): Long
    fun getCommits(): Long
    fun getRollbacks(): Long
}

/**
 * JMX MBean implementation.
 */
class DatabaseMetricsMBeanImpl(
    private val metrics: DatabaseMetrics
) : DatabaseMetricsMBean {

    private fun getSummary(): DatabaseMetricsSummary = metrics.getSummary()

    override fun getActiveConnections(): Int = getSummary().activeConnections
    override fun getIdleConnections(): Int = getSummary().idleConnections
    override fun getTotalConnections(): Int = getSummary().totalConnections
    override fun getTotalQueries(): Long = getSummary().totalQueries
    override fun getQueryErrors(): Long = getSummary().queryErrors
    override fun getAverageQueryTimeMs(): Double = getSummary().averageQueryTime / 1_000_000.0
    override fun getTotalTransactions(): Long = getSummary().totalTransactions
    override fun getCommits(): Long = getSummary().commits
    override fun getRollbacks(): Long = getSummary().rollbacks
}

/**
 * JMX exporter for metrics.
 */
class JmxExporter {
    private val mBeanServer = ManagementFactory.getPlatformMBeanServer()

    /**
     * Register database metrics MBean.
     */
    fun registerDatabaseMetrics(metrics: DatabaseMetrics, name: String = "korm:type=DatabaseMetrics") {
        val mBean = DatabaseMetricsMBeanImpl(metrics)
        val objectName = ObjectName(name)

        try {
            if (mBeanServer.isRegistered(objectName)) {
                mBeanServer.unregisterMBean(objectName)
            }
            mBeanServer.registerMBean(mBean, objectName)
        } catch (e: Exception) {
            throw RuntimeException("Failed to register JMX MBean", e)
        }
    }

    /**
     * Unregister MBean.
     */
    fun unregister(name: String) {
        val objectName = ObjectName(name)
        if (mBeanServer.isRegistered(objectName)) {
            mBeanServer.unregisterMBean(objectName)
        }
    }
}
