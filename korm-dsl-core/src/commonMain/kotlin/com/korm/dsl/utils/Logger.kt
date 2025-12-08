// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/utils/Logger.kt

package com.korm.dsl.utils

import org.slf4j.LoggerFactory

/**
 * Internal logger for KORM.
 */
internal object KormLogger {
    private val logger = LoggerFactory.getLogger("KORM")

    fun debug(message: String) {
        logger.debug(message)
    }

    fun info(message: String) {
        logger.info(message)
    }

    fun warn(message: String) {
        logger.warn(message)
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message)
        }
    }

    fun logQuery(sql: String, executionTimeMs: Long? = null) {
        if (logger.isDebugEnabled) {
            val timeInfo = executionTimeMs?.let { " (${it}ms)" } ?: ""
            logger.debug("SQL$timeInfo: $sql")
        }
    }
}

/**
 * Query logger interface.
 */
interface QueryLogger {
    fun logQuery(sql: String, parameters: Map<String, Any?> = emptyMap(), executionTimeMs: Long? = null)
    fun logError(sql: String, error: Throwable)
}

/**
 * Default query logger implementation.
 */
class DefaultQueryLogger : QueryLogger {
    private val logger = LoggerFactory.getLogger("KORM.Query")

    override fun logQuery(sql: String, parameters: Map<String, Any?>, executionTimeMs: Long?) {
        if (logger.isDebugEnabled) {
            val timeInfo = executionTimeMs?.let { " (${it}ms)" } ?: ""
            val paramInfo = if (parameters.isNotEmpty()) {
                "\nParameters: $parameters"
            } else ""
            logger.debug("Query$timeInfo: $sql$paramInfo")
        }
    }

    override fun logError(sql: String, error: Throwable) {
        logger.error("Query failed: $sql", error)
    }
}

/**
 * No-op query logger.
 */
class NoOpQueryLogger : QueryLogger {
    override fun logQuery(sql: String, parameters: Map<String, Any?>, executionTimeMs: Long?) {
        // Do nothing
    }

    override fun logError(sql: String, error: Throwable) {
        // Do nothing
    }
}
