// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/micrometer/MicrometerBridge.kt

package com.korm.dsl.monitoring.micrometer

import com.korm.dsl.monitoring.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer as MicrometerTimer

/**
 * Bridge between KORM metrics and Micrometer.
 */
class MicrometerBridge(
    private val meterRegistry: MeterRegistry
) {

    /**
     * Create a counter.
     */
    fun counter(name: String, tags: Map<String, String> = emptyMap()): Counter {
        val micrometerCounter = meterRegistry.counter(name, toTags(tags))

        return object : Counter(name, tags) {
            override fun increment(amount: Long) {
                super.increment(amount)
                micrometerCounter.increment(amount.toDouble())
            }
        }
    }

    /**
     * Create a gauge.
     */
    fun gauge(name: String, tags: Map<String, String> = emptyMap()): SimpleGauge {
        val gauge = SimpleGauge(name, tags)

        meterRegistry.gauge(name, toTags(tags), gauge) { it.value() }

        return gauge
    }

    /**
     * Create a timer.
     */
    fun timer(name: String, tags: Map<String, String> = emptyMap()): Timer {
        val micrometerTimer = meterRegistry.timer(name, toTags(tags))

        return object : Timer(name, tags) {
            override fun record(nanos: Long) {
                super.record(nanos)
                micrometerTimer.record(java.time.Duration.ofNanos(nanos))
            }
        }
    }

    /**
     * Record database metrics.
     */
    fun recordDatabaseMetrics(metrics: DatabaseMetrics) {
        // This would integrate with Micrometer's database metrics
        // For now, just expose key metrics

        meterRegistry.gauge("db.connections.active", metrics.getSummary().activeConnections.toDouble())
        meterRegistry.gauge("db.connections.idle", metrics.getSummary().idleConnections.toDouble())
    }

    private fun toTags(tags: Map<String, String>): List<Tag> {
        return tags.map { (key, value) -> Tag.of(key, value) }
    }
}
