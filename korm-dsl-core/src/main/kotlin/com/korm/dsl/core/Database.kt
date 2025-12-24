package com.korm.dsl.core

import com.korm.dsl.dialect.Dialect
import java.sql.Connection
import java.sql.ResultSet

class Database(
    val dialect: Dialect,
    private val connectionPool: ConnectionPool
) : AutoCloseable {

    fun <T> useConnection(block: (Connection) -> T): T {
        return connectionPool.getConnection().use(block)
    }

    override fun close() {
        connectionPool.close()
    }
}

fun <T> ResultSet.map(mapper: (ResultSet) -> T): List<T> {
    val results = mutableListOf<T>()
    while (next()) {
        results.add(mapper(this))
    }
    return results
}