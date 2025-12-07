// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/config/AppConfig.kt

package com.korm.examples.enterprise.config

import com.typesafe.config.ConfigFactory
import java.time.Duration

data class AppConfig(
    val server: ServerConfig,
    val database: DatabaseConfig,
    val cache: CacheConfig,
    val monitoring: MonitoringConfig,
    val security: SecurityConfig
) {
    companion object {
        fun load(): AppConfig {
            val config = ConfigFactory.load()

            return AppConfig(
                server = ServerConfig(
                    port = config.getInt("server.port"),
                    host = config.getString("server.host")
                ),
                database = DatabaseConfig(
                    url = config.getString("database.url"),
                    driver = config.getString("database.driver"),
                    user = config.getString("database.user"),
                    password = config.getString("database.password"),
                    poolSize = config.getInt("database.poolSize"),
                    connectionTimeout = config.getLong("database.connectionTimeout"),
                    maxLifetime = config.getLong("database.maxLifetime")
                ),
                cache = CacheConfig(
                    maxSize = config.getInt("cache.maxSize"),
                    ttlMinutes = config.getLong("cache.ttlMinutes")
                ),
                monitoring = MonitoringConfig(
                    enabled = config.getBoolean("monitoring.enabled"),
                    slowQueryThresholdMs = config.getLong("monitoring.slowQueryThresholdMs"),
                    metricsInterval = config.getLong("monitoring.metricsInterval")
                ),
                security = SecurityConfig(
                    jwtSecret = config.getString("security.jwtSecret"),
                    tokenExpiration = config.getLong("security.tokenExpiration")
                )
            )
        }
    }
}

data class ServerConfig(
    val port: Int,
    val host: String
)

data class DatabaseConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
    val poolSize: Int,
    val connectionTimeout: Long,
    val maxLifetime: Long
)

data class CacheConfig(
    val maxSize: Int,
    val ttlMinutes: Long
)

data class MonitoringConfig(
    val enabled: Boolean,
    val slowQueryThresholdMs: Long,
    val metricsInterval: Long
)

data class SecurityConfig(
    val jwtSecret: String,
    val tokenExpiration: Long
)
