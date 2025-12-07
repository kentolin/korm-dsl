// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/ConnectionPool.kt

package com.korm.dsl.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

/**
 * Connection pool configuration.
 */
data class ConnectionPoolConfig(
    val maximumPoolSize: Int = 10,
    val minimumIdle: Int = 2,
    val connectionTimeout: Long = 30000,
    val idleTimeout: Long = 600000,
    val maxLifetime: Long = 1800000,
    val leakDetectionThreshold: Long = 0,
    val autoCommit: Boolean = false,
    val readOnly: Boolean = false,
    val transactionIsolation: String? = null
)

/**
 * Connection pool manager.
 */
class ConnectionPool private constructor(
    private val dataSource: HikariDataSource
) {

    companion object {
        /**
         * Create a connection pool with configuration.
         */
        fun create(
            url: String,
            driver: String,
            user: String,
            password: String,
            config: ConnectionPoolConfig = ConnectionPoolConfig()
        ): ConnectionPool {
            val hikariConfig = HikariConfig().apply {
                jdbcUrl = url
                driverClassName = driver
                username = user
                this.password = password

                maximumPoolSize = config.maximumPoolSize
                minimumIdle = config.minimumIdle
                connectionTimeout = config.connectionTimeout
                idleTimeout = config.idleTimeout
                maxLifetime = config.maxLifetime

                if (config.leakDetectionThreshold > 0) {
                    leakDetectionThreshold = config.leakDetectionThreshold
                }

                isAutoCommit = config.autoCommit
                isReadOnly = config.readOnly

                config.transactionIsolation?.let {
                    this.transactionIsolation = it
                }

                // Performance optimizations
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                addDataSourceProperty("useServerPrepStmts", "true")
            }

            return ConnectionPool(HikariDataSource(hikariConfig))
        }
    }

    /**
     * Get the underlying DataSource.
     */
    fun getDataSource(): DataSource = dataSource

    /**
     * Get pool statistics.
     */
    fun getStats(): PoolStats {
        return PoolStats(
            totalConnections = dataSource.hikariPoolMXBean?.totalConnections ?: 0,
            activeConnections = dataSource.hikariPoolMXBean?.activeConnections ?: 0,
            idleConnections = dataSource.hikariPoolMXBean?.idleConnections ?: 0,
            threadsAwaitingConnection = dataSource.hikariPoolMXBean?.threadsAwaitingConnection ?: 0
        )
    }

    /**
     * Close the connection pool.
     */
    fun close() {
        dataSource.close()
    }
}

/**
 * Pool statistics.
 */
data class PoolStats(
    val totalConnections: Int,
    val activeConnections: Int,
    val idleConnections: Int,
    val threadsAwaitingConnection: Int
)
