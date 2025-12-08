// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/core/DatabaseExtensions.kt

package com.korm.dsl.core

import com.korm.dsl.query.*
import com.korm.dsl.schema.Table
import java.sql.ResultSet

/**
 * Extension functions for Database class.
 */

/**
 * Execute a SELECT query and return a list of results.
 */
inline fun <T> Database.select(
    table: Table,
    crossinline block: SelectQuery.() -> Unit = {},
    crossinline mapper: (ResultSet) -> T
): List<T> {
    return transaction {
        val query = SelectQuery(table).apply(block)
        select(query, mapper)
    }
}

/**
 * Execute a SELECT query and return a single result.
 */
inline fun <T> Database.selectOne(
    table: Table,
    crossinline block: SelectQuery.() -> Unit = {},
    crossinline mapper: (ResultSet) -> T
): T? {
    return select(table, block, mapper).firstOrNull()
}

/**
 * Execute an INSERT query and return generated ID.
 */
inline fun Database.insert(
    table: Table,
    crossinline block: InsertQuery.() -> Unit
): Long {
    return transaction {
        val query = InsertQuery(table).apply(block)
        insert(query)
    }
}

/**
 * Execute an UPDATE query and return number of affected rows.
 */
inline fun Database.update(
    table: Table,
    crossinline block: UpdateQuery.() -> Unit
): Int {
    return transaction {
        val query = UpdateQuery(table).apply(block)
        update(query)
    }
}

/**
 * Execute a DELETE query and return number of affected rows.
 */
inline fun Database.delete(
    table: Table,
    crossinline block: DeleteQuery.() -> Unit
): Int {
    return transaction {
        val query = DeleteQuery(table).apply(block)
        delete(query)
    }
}

/**
 * Check if a table exists.
 */
fun Database.tableExists(tableName: String): Boolean {
    return transaction {
        val sql = """
            SELECT EXISTS (
                SELECT FROM information_schema.tables
                WHERE table_name = '$tableName'
            )
        """.trimIndent()

        var exists = false
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    if (rs.next()) {
                        exists = rs.getBoolean(1)
                    }
                }
            }
        }
        exists
    }
}

/**
 * Execute raw SQL.
 */
fun Database.executeRaw(sql: String): Boolean {
    return transaction {
        execute(sql)
    }
}

/**
 * Execute a query and return a single value.
 */
fun <T> Database.executeScalar(sql: String, extractor: (ResultSet) -> T): T? {
    return transaction {
        var result: T? = null
        getConnection().use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    if (rs.next()) {
                        result = extractor(rs)
                    }
                }
            }
        }
        result
    }
}

/**
 * Count rows in a table.
 */
fun Database.count(table: Table, where: String? = null): Long {
    val sql = if (where != null) {
        "SELECT COUNT(*) FROM ${table.tableName} WHERE $where"
    } else {
        "SELECT COUNT(*) FROM ${table.tableName}"
    }

    return executeScalar(sql) { it.getLong(1) } ?: 0L
}

/**
 * Check if any rows exist matching a condition.
 */
fun Database.exists(table: Table, where: String): Boolean {
    val sql = "SELECT EXISTS(SELECT 1 FROM ${table.tableName} WHERE $where)"
    return executeScalar(sql) { it.getBoolean(1) } ?: false
}
