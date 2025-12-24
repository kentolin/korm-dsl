package com.korm.dsl.core

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class ConnectionPool(
    private val dataSource: HikariDataSource
) : AutoCloseable {

    fun getConnection(): Connection = dataSource.connection

    override fun close() {
        dataSource.close()
    }

    companion object {
        fun create(
            url: String,
            driver: String,
            user: String = "",
            password: String = "",
            maximumPoolSize: Int = 10
        ): ConnectionPool {
            val config = HikariConfig().apply {
                jdbcUrl = url
                driverClassName = driver
                username = user
                this.password = password
                this.maximumPoolSize = maximumPoolSize
                isAutoCommit = true
            }

            return ConnectionPool(HikariDataSource(config))
        }
    }
}