// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/prometheus/PrometheusExporter.kt

package com.korm.dsl.monitoring.prometheus

import com.korm.dsl.monitoring.*
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.Counter as PrometheusCounter
import io.prometheus.client.Gauge as PrometheusGauge
import io.prometheus.client.Histogram as PrometheusHistogram
import io.prometheus.client.Summary as PrometheusSummary
import io.prometheus.client.exporter.common.TextFormat
import java.io.StringWriter

/**
 * Prometheus metrics exporter.
 */
class PrometheusExporter(
    private val registry: MetricsRegistry = GlobalMetricsRegistry.registry,
    private val prometheusRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
) {
    private val prometheusMetrics = mutableMapOf<String, io.prometheus.client.Collector>()

    /**
     * Export metrics in Prometheus format.
     */
    fun export(): String {
        updatePrometheusMetrics()

        val writer = StringWriter()
        TextFormat.write004(writer, prometheusRegistry.metricFamilySamples())
        return writer.toString()
    }

    /**
     * Update Prometheus metrics from registry.
     */
    private fun updatePrometheusMetrics() {
        registry.getMetrics().forEach { metric ->
            when (metric) {
                is Counter -> exportCounter(metric)
                is SimpleGauge -> exportGauge(metric)
                is Gauge -> exportGauge(metric)
                is Histogram -> exportHistogram(metric)
                is Timer -> exportTimer(metric)
                is Summary -> exportSummary(metric)
            }
        }
    }

    private fun exportCounter(counter: Counter) {
        val prometheus = prometheusMetrics.getOrPut(counter.name) {
            PrometheusCounter.build()
                .name(sanitizeName(counter.name))
                .help("Counter: ${counter.name}")
                .labelNames(*counter.tags.keys.toTypedArray())
                .register(prometheusRegistry)
        } as PrometheusCounter

        if (counter.tags.isEmpty()) {
            prometheus.inc(counter.count().toDouble())
        } else {
            prometheus.labels(*counter.tags.values.toTypedArray()).inc(counter.count().toDouble())
        }
    }

    private fun exportGauge(gauge: SimpleGauge) {
        val prometheus = prometheusMetrics.getOrPut(gauge.name) {
            PrometheusGauge.build()
                .name(sanitizeName(gauge.name))
                .help("Gauge: ${gauge.name}")
                .labelNames(*gauge.tags.keys.toTypedArray())
                .register(prometheusRegistry)
        } as PrometheusGauge

        if (gauge.tags.isEmpty()) {
            prometheus.set(gauge.value())
        } else {
            prometheus.labels(*gauge.tags.values.toTypedArray()).set(gauge.value())
        }
    }

    private fun exportGauge(gauge: Gauge) {
        val prometheus = prometheusMetrics.getOrPut(gauge.name) {
            PrometheusGauge.build()
                .name(sanitizeName(gauge.name))
                .help("Gauge: ${gauge.name}")
                .labelNames(*gauge.tags.keys.toTypedArray())
                .register(prometheusRegistry)
        } as PrometheusGauge

        if (gauge.tags.isEmpty()) {
            prometheus.set(gauge.value())
        } else {
            prometheus.labels(*gauge.tags.values.toTypedArray()).set(gauge.value())
        }
    }

    private fun exportHistogram(histogram: Histogram) {
        val prometheus = prometheusMetrics.getOrPut(histogram.name) {
            PrometheusHistogram.build()
                .name(sanitizeName(histogram.name))
                .help("Histogram: ${histogram.name}")
                .labelNames(*histogram.tags.keys.toTypedArray())
                .register(prometheusRegistry)
        } as PrometheusHistogram

        // Prometheus histograms are cumulative, this is a simplified export
        if (histogram.tags.isEmpty()) {
            prometheus.observe(histogram.mean())
        } else {
            prometheus.labels(*histogram.tags.values.toTypedArray()).observe(histogram.mean())
        }
    }

    private fun exportTimer(timer: Timer) {
        val prometheus = prometheusMetrics.getOrPut(timer.name) {
            PrometheusSummary.build()
                .name(sanitizeName(timer.name))
                .help("Timer: ${timer.name}")
                .labelNames(*timer.tags.keys.toTypedArray())
                .register(prometheusRegistry)
        } as PrometheusSummary

        // Convert nanoseconds to seconds for Prometheus
        val seconds = timer.mean() / 1_000_000_000.0

        if (timer.tags.isEmpty()) {
            prometheus.observe(seconds)
        } else {
            prometheus.labels(*timer.tags.values.toTypedArray()).observe(seconds)
        }
    }

    private fun exportSummary(summary: Summary) {
        val prometheus = prometheusMetrics.getOrPut(summary.name) {
            PrometheusSummary.build()
                .name(sanitizeName(summary.name))
                .help("Summary: ${summary.name}")
                .labelNames(*summary.tags.keys.toTypedArray())
                .register(prometheusRegistry)
        } as PrometheusSummary

        if (summary.tags.isEmpty()) {
            prometheus.observe(summary.mean())
        } else {
            prometheus.labels(*summary.tags.values.toTypedArray()).observe(summary.mean())
        }
    }

    /**
     * Sanitize metric name for Prometheus.
     */
    private fun sanitizeName(name: String): String {
        return name.replace("[^a-zA-Z0-9_:]".toRegex(), "_")
    }
}
