// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/InsertQuery.kt

package com.korm.dsl.query

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import java.sql.PreparedStatement

/**
 * Builder for INSERT queries.
 */
class InsertQuery(private val table: Table) {
    private val values = mutableMapOf<Column<*>, Any?>()

    /**
     * Set value for a column.
     */
    operator fun <T> set(column: Column<T>, value: T?) {
        values[column] = value
    }

    /**
     * Generate SQL for this query.
     */
    fun toSql(): String {
        if (values.isEmpty()) {
            throw IllegalStateException("Cannot insert without any values")
        }

        val columns = values.keys.joinToString(", ") { it.name }
        val placeholders = values.keys.joinToString(", ") { "?" }

        return "INSERT INTO ${table.tableName} ($columns) VALUES ($placeholders)"
    }

    /**
     * Bind parameters to prepared statement.
     */
    fun bindParameters(statement: PreparedStatement) {
        values.entries.forEachIndexed { index, entry ->
            @Suppress("UNCHECKED_CAST")
            val column = entry.key as Column<Any?>
            column.type.bindTo(statement, index + 1, entry.value)
        }
    }
}
