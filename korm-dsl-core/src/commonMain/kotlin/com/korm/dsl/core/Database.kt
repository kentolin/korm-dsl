// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/Database.kt

package com.korm.dsl.core

import com.korm.dsl.schema.Table
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import javax.sql.DataSource

/**
 * Main entry point for KORM DSL database operations.
 * Manages database connections and provides transaction support.
 */
class Database private constructor(
    private val dataSource: DataSource
) {
    companion object {
        /**
         * Connect to a database using JDBC URL and credentials.
         */
        fun connect(
            url: String,
            driver: String,
            user: String,
            password: String,
            maximumPoolSize: Int = 10
        ): Database {
            val config = HikariConfig().apply {
                jdbcUrl = url
                driverClassName = driver
                username = user
                this.password = password
                this.maximumPoolSize = maximumPoolSize
                isAutoCommit = false
            }

            return Database(HikariDataSource(config))
        }

        /**
         * Connect using an existing DataSource.
         */
        fun connect(dataSource: DataSource): Database {
            return Database(dataSource)
        }
    }

    /**
     * Get a connection from the pool.
     */
    fun getConnection(): Connection {
        return dataSource.connection
    }

    /**
     * Execute a block within a transaction.
     */
    fun <T> transaction(block: Transaction.() -> T): T {
        return getConnection().use { connection ->
            val transaction = Transaction(connection)
            try {
                val result = transaction.block()
                connection.commit()
                result
            } catch (e: Exception) {
                connection.rollback()
                throw e
            }
        }
    }

    /**
     * Create tables in the database.
     */
    fun createTables(vararg tables: Table) {
        transaction {
            tables.forEach { table ->
                execute(table.createStatement())
            }
        }
    }

    /**
     * Drop tables from the database.
     */
    fun dropTables(vararg tables: Table) {
        transaction {
            tables.forEach { table ->
                execute("DROP TABLE IF EXISTS ${table.tableName} CASCADE")
            }
        }
    }

    /**
     * Close the database connection pool.
     */
    fun close() {
        if (dataSource is HikariDataSource) {
            dataSource.close()
        }
    }
}
