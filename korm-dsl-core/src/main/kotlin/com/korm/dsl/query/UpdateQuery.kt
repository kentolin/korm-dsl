// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/UpdateQuery.kt

package com.korm.dsl.query

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import java.sql.PreparedStatement

/**
 * Builder for UPDATE queries.
 */
class UpdateQuery(private val table: Table) {
    private val updates = mutableMapOf<Column<*>, Any?>()
    private val whereClauses = mutableListOf<String>()

    /**
     * Set value for a column.
     */
    operator fun <T> set(column: Column<T>, value: T?) {
        updates[column] = value
    }

    /**
     * Add WHERE clause.
     */
    fun where(condition: String): UpdateQuery {
        whereClauses.add(condition)
        return this
    }

    /**
     * Add AND condition.
     */
    fun and(condition: String): UpdateQuery {
        if (whereClauses.isEmpty()) {
            throw IllegalStateException("Cannot add AND without a WHERE clause")
        }
        whereClauses.add("AND $condition")
        return this
    }

    /**
     * Generate SQL for this query.
     */
    fun toSql(): String {
        if (updates.isEmpty()) {
            throw IllegalStateException("Cannot update without any values")
        }

        val setClause = updates.keys.joinToString(", ") { "${it.name} = ?" }
        val sql = StringBuilder("UPDATE ${table.tableName} SET $setClause")

        if (whereClauses.isNotEmpty()) {
            sql.append(" WHERE ").append(whereClauses.joinToString(" "))
        }

        return sql.toString()
    }

    /**
     * Bind parameters to prepared statement.
     */
    fun bindParameters(statement: PreparedStatement) {
        updates.entries.forEachIndexed { index, entry ->
            @Suppress("UNCHECKED_CAST")
            val column = entry.key as Column<Any?>
            column.type.bindTo(statement, index + 1, entry.value)
        }
    }
}
