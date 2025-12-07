// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/MetricsRegistry.kt

package com.korm.dsl.monitoring

import java.util.concurrent.ConcurrentHashMap

/**
 * Registry for managing metrics.
 */
class MetricsRegistry {
    private val metrics = ConcurrentHashMap<String, Metric>()

    /**
     * Register a metric.
     */
    fun register(metric: Metric): Metric {
        val key = metricKey(metric.name, metric.tags)
        metrics[key] = metric
        return metric
    }

    /**
     * Get or create a counter.
     */
    fun counter(name: String, tags: Map<String, String> = emptyMap()): Counter {
        val key = metricKey(name, tags)
        return metrics.getOrPut(key) {
            Counter(name, tags)
        } as Counter
    }

    /**
     * Get or create a simple gauge.
     */
    fun gauge(name: String, tags: Map<String, String> = emptyMap()): SimpleGauge {
        val key = metricKey(name, tags)
        return metrics.getOrPut(key) {
            SimpleGauge(name, tags)
        } as SimpleGauge
    }

    /**
     * Get or create a gauge with supplier.
     */
    fun gauge(
        name: String,
        tags: Map<String, String> = emptyMap(),
        supplier: () -> Double
    ): Gauge {
        val key = metricKey(name, tags)
        return metrics.getOrPut(key) {
            Gauge(name, tags, supplier)
        } as Gauge
    }

    /**
     * Get or create a histogram.
     */
    fun histogram(name: String, tags: Map<String, String> = emptyMap()): Histogram {
        val key = metricKey(name, tags)
        return metrics.getOrPut(key) {
            Histogram(name, tags)
        } as Histogram
    }

    /**
     * Get or create a timer.
     */
    fun timer(name: String, tags: Map<String, String> = emptyMap()): Timer {
        val key = metricKey(name, tags)
        return metrics.getOrPut(key) {
            Timer(name, tags)
        } as Timer
    }

    /**
     * Get or create a summary.
     */
    fun summary(name: String, tags: Map<String, String> = emptyMap()): Summary {
        val key = metricKey(name, tags)
        return metrics.getOrPut(key) {
            Summary(name, tags)
        } as Summary
    }

    /**
     * Get a metric by name and tags.
     */
    fun getMetric(name: String, tags: Map<String, String> = emptyMap()): Metric? {
        val key = metricKey(name, tags)
        return metrics[key]
    }

    /**
     * Get all metrics.
     */
    fun getMetrics(): Collection<Metric> {
        return metrics.values
    }

    /**
     * Get metrics by type.
     */
    fun getMetricsByType(type: MetricType): List<Metric> {
        return metrics.values.filter { it.type == type }
    }

    /**
     * Remove a metric.
     */
    fun remove(name: String, tags: Map<String, String> = emptyMap()): Boolean {
        val key = metricKey(name, tags)
        return metrics.remove(key) != null
    }

    /**
     * Clear all metrics.
     */
    fun clear() {
        metrics.clear()
    }

    /**
     * Reset all metrics.
     */
    fun reset() {
        metrics.values.forEach { it.reset() }
    }

    private fun metricKey(name: String, tags: Map<String, String>): String {
        return if (tags.isEmpty()) {
            name
        } else {
            val tagString = tags.entries
                .sortedBy { it.key }
                .joinToString(",") { "${it.key}=${it.value}" }
            "$name{$tagString}"
        }
    }
}

/**
 * Global metrics registry.
 */
object GlobalMetricsRegistry {
    private val registry = MetricsRegistry()

    fun counter(name: String, tags: Map<String, String> = emptyMap()): Counter {
        return registry.counter(name, tags)
    }

    fun gauge(name: String, tags: Map<String, String> = emptyMap()): SimpleGauge {
        return registry.gauge(name, tags)
    }

    fun gauge(
        name: String,
        tags: Map<String, String> = emptyMap(),
        supplier: () -> Double
    ): Gauge {
        return registry.gauge(name, tags, supplier)
    }

    fun histogram(name: String, tags: Map<String, String> = emptyMap()): Histogram {
        return registry.histogram(name, tags)
    }

    fun timer(name: String, tags: Map<String, String> = emptyMap()): Timer {
        return registry.timer(name, tags)
    }

    fun summary(name: String, tags: Map<String, String> = emptyMap()): Summary {
        return registry.summary(name, tags)
    }

    fun getMetrics(): Collection<Metric> {
        return registry.getMetrics()
    }

    fun clear() {
        registry.clear()
    }

    fun reset() {
        registry.reset()
    }
}
