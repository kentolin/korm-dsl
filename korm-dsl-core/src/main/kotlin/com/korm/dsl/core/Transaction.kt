// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/Transaction.kt

package com.korm.dsl.core

import com.korm.dsl.query.*
import com.korm.dsl.schema.Table
import java.sql.Connection
import java.sql.ResultSet

/**
 * Represents a database transaction with query execution capabilities.
 */
class Transaction(private val connection: Connection) {

    /**
     * Execute a raw SQL statement.
     */
    fun execute(sql: String): Boolean {
        return connection.createStatement().use { statement ->
            statement.execute(sql)
        }
    }

    /**
     * Execute a SELECT query and map results.
     */
    fun <T> select(query: SelectQuery, mapper: (ResultSet) -> T): List<T> {
        val sql = query.toSql()
        return connection.prepareStatement(sql).use { statement ->
            val results = mutableListOf<T>()
            val resultSet = statement.executeQuery()
            while (resultSet.next()) {
                results.add(mapper(resultSet))
            }
            results
        }
    }

    /**
     * Execute an INSERT query.
     */
    fun insert(query: InsertQuery): Long {
        val sql = query.toSql()
        return connection.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS).use { statement ->
            query.bindParameters(statement)
            statement.executeUpdate()

            val keys = statement.generatedKeys
            if (keys.next()) {
                keys.getLong(1)
            } else {
                throw IllegalStateException("No generated key returned")
            }
        }
    }

    /**
     * Execute an UPDATE query.
     */
    fun update(query: UpdateQuery): Int {
        val sql = query.toSql()
        return connection.prepareStatement(sql).use { statement ->
            query.bindParameters(statement)
            statement.executeUpdate()
        }
    }

    /**
     * Execute a DELETE query.
     */
    fun delete(query: DeleteQuery): Int {
        val sql = query.toSql()
        return connection.prepareStatement(sql).use { statement ->
            query.bindParameters(statement)
            statement.executeUpdate()
        }
    }

    /**
     * Create a SELECT query builder.
     */
    fun from(table: Table): SelectQuery {
        return SelectQuery(table)
    }

    /**
     * Create an INSERT query builder.
     */
    fun insertInto(table: Table, block: InsertQuery.() -> Unit): InsertQuery {
        return InsertQuery(table).apply(block)
    }

    /**
     * Create an UPDATE query builder.
     */
    fun update(table: Table, block: UpdateQuery.() -> Unit): UpdateQuery {
        return UpdateQuery(table).apply(block)
    }

    /**
     * Create a DELETE query builder.
     */
    fun deleteFrom(table: Table, block: DeleteQuery.() -> Unit): DeleteQuery {
        return DeleteQuery(table).apply(block)
    }
}
