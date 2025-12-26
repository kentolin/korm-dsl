package com.korm.dsl.monitoring

/**
 * Configuration for monitoring
 */
data class MonitoringConfig(
    /**
     * Enable query monitoring
     */
    val enableQueryMonitoring: Boolean = true,

    /**
     * Enable slow query logging
     */
    val enableSlowQueryLog: Boolean = true,

    /**
     * Slow query threshold in milliseconds
     */
    val slowQueryThresholdMs: Long = 1000,

    /**
     * Enable console monitor
     */
    val enableConsoleMonitor: Boolean = false,

    /**
     * Show all queries in console (false = only slow queries)
     */
    val consoleShowAllQueries: Boolean = false,

    /**
     * Enable performance profiler
     */
    val enablePerformanceProfiler: Boolean = true,

    /**
     * Enable connection monitoring
     */
    val enableConnectionMonitoring: Boolean = true,

    /**
     * Enable transaction monitoring
     */
    val enableTransactionMonitoring: Boolean = true,

    /**
     * Enable batch operation monitoring
     */
    val enableBatchMonitoring: Boolean = true
) {
    companion object {
        /**
         * Default configuration
         */
        val DEFAULT = MonitoringConfig()

        /**
         * Development configuration - shows everything
         */
        val DEVELOPMENT = MonitoringConfig(
            enableConsoleMonitor = true,
            consoleShowAllQueries = true,
            slowQueryThresholdMs = 500
        )

        /**
         * Production configuration - minimal overhead
         */
        val PRODUCTION = MonitoringConfig(
            enableConsoleMonitor = false,
            consoleShowAllQueries = false,
            slowQueryThresholdMs = 1000
        )

        /**
         * Disabled configuration - no monitoring
         */
        val DISABLED = MonitoringConfig(
            enableQueryMonitoring = false,
            enableSlowQueryLog = false,
            enableConsoleMonitor = false,
            enablePerformanceProfiler = false,
            enableConnectionMonitoring = false,
            enableTransactionMonitoring = false,
            enableBatchMonitoring = false
        )
    }
}

/**
 * Monitoring facade - simplified interface for setting up monitoring
 */
class Monitoring(
    val config: MonitoringConfig = MonitoringConfig.DEFAULT
) {
    val eventBus = EventBus.global
    val queryMonitor = QueryMonitor(eventBus)

    // Listeners
    private var slowQueryLogger: SlowQueryLogger? = null
    private var consoleMonitor: ConsoleMonitor? = null
    private var performanceProfiler: PerformanceProfiler? = null

    init {
        setup()
    }

    private fun setup() {
        // Setup slow query logger
        if (config.enableSlowQueryLog) {
            slowQueryLogger = SlowQueryLogger(config.slowQueryThresholdMs)
            eventBus.addListener(slowQueryLogger!!)
        }

        // Setup console monitor
        if (config.enableConsoleMonitor) {
            consoleMonitor = ConsoleMonitor(
                showAllQueries = config.consoleShowAllQueries,
                slowQueryThreshold = config.slowQueryThresholdMs
            )
            eventBus.addListener(consoleMonitor!!)
        }

        // Setup performance profiler
        if (config.enablePerformanceProfiler) {
            performanceProfiler = PerformanceProfiler()
            eventBus.addListener(performanceProfiler!!)
        }
    }

    /**
     * Get slow query statistics
     */
    fun getSlowQueryStats(): SlowQueryStats? {
        return slowQueryLogger?.getStats()
    }

    /**
     * Get performance report
     */
    fun getPerformanceReport(): PerformanceReport? {
        return performanceProfiler?.getReport()
    }

    /**
     * Print performance report
     */
    fun printPerformanceReport() {
        performanceProfiler?.printReport()
    }

    /**
     * Reset all statistics
     */
    fun reset() {
        slowQueryLogger?.resetStats()
        performanceProfiler?.reset()
    }

    /**
     * Add a custom listener
     */
    fun addListener(listener: EventListener) {
        eventBus.addListener(listener)
    }

    /**
     * Remove a listener
     */
    fun removeListener(listener: EventListener) {
        eventBus.removeListener(listener)
    }

    /**
     * Shutdown monitoring
     */
    fun shutdown() {
        eventBus.clearListeners()
    }

    companion object {
        /**
         * Global monitoring instance
         */
        private var globalInstance: Monitoring? = null

        /**
         * Get or create global monitoring instance
         */
        fun global(config: MonitoringConfig = MonitoringConfig.DEFAULT): Monitoring {
            if (globalInstance == null) {
                globalInstance = Monitoring(config)
            }
            return globalInstance!!
        }

        /**
         * Configure global monitoring
         */
        fun configure(config: MonitoringConfig) {
            globalInstance?.shutdown()
            globalInstance = Monitoring(config)
        }

        /**
         * Reset global monitoring
         */
        fun reset() {
            globalInstance?.reset()
        }

        /**
         * Shutdown global monitoring
         */
        fun shutdown() {
            globalInstance?.shutdown()
            globalInstance = null
        }
    }
}

/**
 * DSL for monitoring configuration
 */
class MonitoringConfigBuilder {
    var enableQueryMonitoring: Boolean = true
    var enableSlowQueryLog: Boolean = true
    var slowQueryThresholdMs: Long = 1000
    var enableConsoleMonitor: Boolean = false
    var consoleShowAllQueries: Boolean = false
    var enablePerformanceProfiler: Boolean = true
    var enableConnectionMonitoring: Boolean = true
    var enableTransactionMonitoring: Boolean = true
    var enableBatchMonitoring: Boolean = true

    fun build(): MonitoringConfig {
        return MonitoringConfig(
            enableQueryMonitoring = enableQueryMonitoring,
            enableSlowQueryLog = enableSlowQueryLog,
            slowQueryThresholdMs = slowQueryThresholdMs,
            enableConsoleMonitor = enableConsoleMonitor,
            consoleShowAllQueries = consoleShowAllQueries,
            enablePerformanceProfiler = enablePerformanceProfiler,
            enableConnectionMonitoring = enableConnectionMonitoring,
            enableTransactionMonitoring = enableTransactionMonitoring,
            enableBatchMonitoring = enableBatchMonitoring
        )
    }
}

/**
 * Create monitoring configuration using DSL
 */
fun monitoringConfig(block: MonitoringConfigBuilder.() -> Unit): MonitoringConfig {
    val builder = MonitoringConfigBuilder()
    builder.block()
    return builder.build()
}
