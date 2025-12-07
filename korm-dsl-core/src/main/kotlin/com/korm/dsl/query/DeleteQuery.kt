// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/DeleteQuery.kt

package com.korm.dsl.query

import com.korm.dsl.schema.Table
import java.sql.PreparedStatement

/**
 * Builder for DELETE queries.
 */
class DeleteQuery(private val table: Table) {
    private val whereClauses = mutableListOf<String>()

    /**
     * Add WHERE clause.
     */
    fun where(condition: String): DeleteQuery {
        whereClauses.add(condition)
        return this
    }

    /**
     * Add AND condition.
     */
    fun and(condition: String): DeleteQuery {
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
        val sql = StringBuilder("DELETE FROM ${table.tableName}")

        if (whereClauses.isNotEmpty()) {
            sql.append(" WHERE ").append(whereClauses.joinToString(" "))
        }

        return sql.toString()
    }

    /**
     * Bind parameters to prepared statement.
     */
    fun bindParameters(statement: PreparedStatement) {
        // Placeholder for parameter binding if needed
    }
}
