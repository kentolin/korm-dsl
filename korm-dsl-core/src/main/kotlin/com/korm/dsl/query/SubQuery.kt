package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

/**
 * Represents a subquery that can be used in WHERE clauses
 */
class Subquery<T : Table> internal constructor(
    internal val sql: String,
    internal val params: List<Any?>
) {
    companion object {
        /**
         * Create a subquery from a select query
         */
        fun <T : Table> from(selectQuery: SelectQuery<T>): Subquery<T> {
            val (sql, params) = selectQuery.toSubquerySql()
            return Subquery(sql, params)
        }
    }
}

/**
 * Extension for SelectQuery to support subquery operations
 */
internal fun <T : Table> SelectQuery<T>.toSubquerySql(): Pair<String, List<Any?>> {
    // Build SQL without outer parentheses - those are added by the WHERE clause
    return buildSqlForSubquery() to getParams()
}

// Make this internal method available
internal fun <T : Table> SelectQuery<T>.buildSubquerySqlInternal(): String {
    return buildSqlForSubquery()
}

/**
 * Add whereIn support to SelectQuery
 */
fun <T : Table, V : Any> SelectQuery<T>.whereIn(
    column: Column<V>,
    subquery: Subquery<*>
): SelectQuery<T> {
    return this.whereRaw(
        "${column.table.tableName}.${column.name} IN (${subquery.sql})",
        *subquery.params.toTypedArray()
    )
}

/**
 * Add whereNotIn support
 */
fun <T : Table, V : Any> SelectQuery<T>.whereNotIn(
    column: Column<V>,
    subquery: Subquery<*>
): SelectQuery<T> {
    return this.whereRaw(
        "${column.table.tableName}.${column.name} NOT IN (${subquery.sql})",
        *subquery.params.toTypedArray()
    )
}

/**
 * Add whereExists support
 */
fun <T : Table> SelectQuery<T>.whereExists(subquery: Subquery<*>): SelectQuery<T> {
    return this.whereRaw(
        "EXISTS (${subquery.sql})",
        *subquery.params.toTypedArray()
    )
}

/**
 * Add whereNotExists support
 */
fun <T : Table> SelectQuery<T>.whereNotExists(subquery: Subquery<*>): SelectQuery<T> {
    return this.whereRaw(
        "NOT EXISTS (${subquery.sql})",
        *subquery.params.toTypedArray()
    )
}