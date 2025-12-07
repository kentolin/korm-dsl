// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/SelectQuery.kt

package com.korm.dsl.query

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

/**
 * Builder for SELECT queries.
 */
class SelectQuery(private val table: Table) {
    private val selectedColumns = mutableListOf<Column<*>>()
    private val whereClauses = mutableListOf<String>()
    private val orderByClauses = mutableListOf<String>()
    private var limitValue: Int? = null
    private var offsetValue: Int? = null
    private val joinClauses = mutableListOf<String>()

    /**
     * Select specific columns.
     */
    fun select(vararg columns: Column<*>): SelectQuery {
        selectedColumns.addAll(columns)
        return this
    }

    /**
     * Add WHERE clause.
     */
    fun where(condition: String): SelectQuery {
        whereClauses.add(condition)
        return this
    }

    /**
     * Add AND condition to WHERE clause.
     */
    fun and(condition: String): SelectQuery {
        if (whereClauses.isEmpty()) {
            throw IllegalStateException("Cannot add AND without a WHERE clause")
        }
        whereClauses.add("AND $condition")
        return this
    }

    /**
     * Add OR condition to WHERE clause.
     */
    fun or(condition: String): SelectQuery {
        if (whereClauses.isEmpty()) {
            throw IllegalStateException("Cannot add OR without a WHERE clause")
        }
        whereClauses.add("OR $condition")
        return this
    }

    /**
     * Add ORDER BY clause.
     */
    fun orderBy(column: Column<*>, direction: String = "ASC"): SelectQuery {
        orderByClauses.add("${column.name} $direction")
        return this
    }

    /**
     * Set LIMIT.
     */
    fun limit(count: Int): SelectQuery {
        limitValue = count
        return this
    }

    /**
     * Set OFFSET.
     */
    fun offset(count: Int): SelectQuery {
        offsetValue = count
        return this
    }

    /**
     * Add INNER JOIN.
     */
    fun innerJoin(
        otherTable: Table,
        onCondition: String
    ): SelectQuery {
        joinClauses.add("INNER JOIN ${otherTable.tableName} ON $onCondition")
        return this
    }

    /**
     * Add LEFT JOIN.
     */
    fun leftJoin(
        otherTable: Table,
        onCondition: String
    ): SelectQuery {
        joinClauses.add("LEFT JOIN ${otherTable.tableName} ON $onCondition")
        return this
    }

    /**
     * Generate SQL for this query.
     */
    fun toSql(): String {
        val columns = if (selectedColumns.isEmpty()) {
            "*"
        } else {
            selectedColumns.joinToString(", ") { it.name }
        }

        val sql = StringBuilder("SELECT $columns FROM ${table.tableName}")

        if (joinClauses.isNotEmpty()) {
            sql.append(" ").append(joinClauses.joinToString(" "))
        }

        if (whereClauses.isNotEmpty()) {
            sql.append(" WHERE ").append(whereClauses.joinToString(" "))
        }

        if (orderByClauses.isNotEmpty()) {
            sql.append(" ORDER BY ").append(orderByClauses.joinToString(", "))
        }

        limitValue?.let { sql.append(" LIMIT $it") }
        offsetValue?.let { sql.append(" OFFSET $it") }

        return sql.toString()
    }
}
