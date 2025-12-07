// korm-dsl/korm-dsl-monitoring/src/main/kotlin/com/korm/dsl/monitoring/Metrics.kt

package com.korm.dsl.monitoring

import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.DoubleAdder

/**
 * Base interface for metrics.
 */
interface Metric {
    /**
     * Get metric name.
     */
    val name: String

    /**
     * Get metric type.
     */
    val type: MetricType

    /**
     * Get metric tags.
     */
    val tags: Map<String, String>

    /**
     * Reset metric.
     */
    fun reset()
}

/**
 * Metric types.
 */
enum class MetricType {
    COUNTER,
    GAUGE,
    HISTOGRAM,
    TIMER,
    SUMMARY
}

/**
 * Counter metric.
 */
class Counter(
    override val name: String,
    override val tags: Map<String, String> = emptyMap()
) : Metric {
    override val type = MetricType.COUNTER

    private val count = AtomicLong(0)

    /**
     * Increment counter.
     */
    fun increment(amount: Long = 1) {
        count.addAndGet(amount)
    }

    /**
     * Get current count.
     */
    fun count(): Long = count.get()

    override fun reset() {
        count.set(0)
    }
}

/**
 * Gauge metric.
 */
class Gauge(
    override val name: String,
    override val tags: Map<String, String> = emptyMap(),
    private val supplier: () -> Double
) : Metric {
    override val type = MetricType.GAUGE

    /**
     * Get current value.
     */
    fun value(): Double = supplier()

    override fun reset() {
        // Gauges typically don't reset as they represent current state
    }
}

/**
 * Simple gauge with settable value.
 */
class SimpleGauge(
    override val name: String,
    override val tags: Map<String, String> = emptyMap()
) : Metric {
    override val type = MetricType.GAUGE

    private val adder = DoubleAdder()

    /**
     * Set gauge value.
     */
    fun set(value: Double) {
        adder.reset()
        adder.add(value)
    }

    /**
     * Increment gauge.
     */
    fun increment(amount: Double = 1.0) {
        adder.add(amount)
    }

    /**
     * Decrement gauge.
     */
    fun decrement(amount: Double = 1.0) {
        adder.add(-amount)
    }

    /**
     * Get current value.
     */
    fun value(): Double = adder.sum()

    override fun reset() {
        adder.reset()
    }
}

/**
 * Histogram metric for tracking distributions.
 */
class Histogram(
    override val name: String,
    override val tags: Map<String, String> = emptyMap()
) : Metric {
    override val type = MetricType.HISTOGRAM

    private val values = mutableListOf<Long>()
    private val count = AtomicLong(0)
    private val sum = AtomicLong(0)

    /**
     * Record a value.
     */
    @Synchronized
    fun record(value: Long) {
        values.add(value)
        count.incrementAndGet()
        sum.addAndGet(value)
    }

    /**
     * Get count of recorded values.
     */
    fun count(): Long = count.get()

    /**
     * Get sum of all values.
     */
    fun sum(): Long = sum.get()

    /**
     * Get mean value.
     */
    fun mean(): Double {
        val c = count()
        return if (c > 0) sum.get().toDouble() / c else 0.0
    }

    /**
     * Get max value.
     */
    @Synchronized
    fun max(): Long = values.maxOrNull() ?: 0

    /**
     * Get min value.
     */
    @Synchronized
    fun min(): Long = values.minOrNull() ?: 0

    /**
     * Get percentile.
     */
    @Synchronized
    fun percentile(percentile: Double): Long {
        if (values.isEmpty()) return 0

        val sorted = values.sorted()
        val index = ((percentile / 100.0) * sorted.size).toInt()
        return sorted[index.coerceIn(0, sorted.size - 1)]
    }

    override fun reset() {
        synchronized(this) {
            values.clear()
            count.set(0)
            sum.set(0)
        }
    }
}

/**
 * Timer metric for tracking duration.
 */
class Timer(
    override val name: String,
    override val tags: Map<String, String> = emptyMap()
) : Metric {
    override val type = MetricType.TIMER

    private val histogram = Histogram(name, tags)

    /**
     * Time a block of code.
     */
    inline fun <T> record(block: () -> T): T {
        val start = System.nanoTime()
        try {
            return block()
        } finally {
            val duration = System.nanoTime() - start
            histogram.record(duration)
        }
    }

    /**
     * Record a duration.
     */
    fun record(duration: Duration) {
        histogram.record(duration.toNanos())
    }

    /**
     * Record nanoseconds.
     */
    fun record(nanos: Long) {
        histogram.record(nanos)
    }

    /**
     * Get count of recorded timings.
     */
    fun count(): Long = histogram.count()

    /**
     * Get total time in nanoseconds.
     */
    fun totalTime(): Long = histogram.sum()

    /**
     * Get mean time in nanoseconds.
     */
    fun mean(): Double = histogram.mean()

    /**
     * Get max time in nanoseconds.
     */
    fun max(): Long = histogram.max()

    /**
     * Get percentile in nanoseconds.
     */
    fun percentile(percentile: Double): Long = histogram.percentile(percentile)

    override fun reset() {
        histogram.reset()
    }
}

/**
 * Summary metric for tracking statistics.
 */
class Summary(
    override val name: String,
    override val tags: Map<String, String> = emptyMap()
) : Metric {
    override val type = MetricType.SUMMARY

    private val count = AtomicLong(0)
    private val sum = DoubleAdder()
    private var max = Double.MIN_VALUE
    private var min = Double.MAX_VALUE

    /**
     * Record a value.
     */
    @Synchronized
    fun record(value: Double) {
        count.incrementAndGet()
        sum.add(value)
        if (value > max) max = value
        if (value < min) min = value
    }

    /**
     * Get count.
     */
    fun count(): Long = count.get()

    /**
     * Get sum.
     */
    fun sum(): Double = sum.sum()

    /**
     * Get mean.
     */
    fun mean(): Double {
        val c = count()
        return if (c > 0) sum() / c else 0.0
    }

    /**
     * Get max.
     */
    fun max(): Double = if (max == Double.MIN_VALUE) 0.0 else max

    /**
     * Get min.
     */
    fun min(): Double = if (min == Double.MAX_VALUE) 0.0 else min

    override fun reset() {
        synchronized(this) {
            count.set(0)
            sum.reset()
            max = Double.MIN_VALUE
            min = Double.MAX_VALUE
        }
    }
}
