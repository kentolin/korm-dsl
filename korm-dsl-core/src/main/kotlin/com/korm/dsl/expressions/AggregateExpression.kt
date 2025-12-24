package com.korm.dsl.expressions

import com.korm.dsl.schema.Column

sealed class AggregateExpression(val alias: String?) {
    abstract fun toSql(): String

    data class Count(val column: Column<*>?, val distinct: Boolean = false, val customAlias: String? = null) : AggregateExpression(customAlias) {
        override fun toSql(): String {
            val col = if (column != null) {
                if (distinct) "DISTINCT ${column.table.tableName}.${column.name}"
                else "${column.table.tableName}.${column.name}"
            } else {
                "*"
            }
            val sql = "COUNT($col)"
            return if (alias != null) "$sql AS $alias" else sql
        }
    }

    data class Sum(val column: Column<*>, val customAlias: String? = null) : AggregateExpression(customAlias) {
        override fun toSql(): String {
            val sql = "SUM(${column.table.tableName}.${column.name})"
            return if (alias != null) "$sql AS $alias" else sql
        }
    }

    data class Avg(val column: Column<*>, val customAlias: String? = null) : AggregateExpression(customAlias) {
        override fun toSql(): String {
            val sql = "AVG(${column.table.tableName}.${column.name})"
            return if (alias != null) "$sql AS $alias" else sql
        }
    }

    data class Max(val column: Column<*>, val customAlias: String? = null) : AggregateExpression(customAlias) {
        override fun toSql(): String {
            val sql = "MAX(${column.table.tableName}.${column.name})"
            return if (alias != null) "$sql AS $alias" else sql
        }
    }

    data class Min(val column: Column<*>, val customAlias: String? = null) : AggregateExpression(customAlias) {
        override fun toSql(): String {
            val sql = "MIN(${column.table.tableName}.${column.name})"
            return if (alias != null) "$sql AS $alias" else sql
        }
    }
}

// Helper functions for creating aggregates
fun count(column: Column<*>? = null, distinct: Boolean = false, alias: String? = null) =
    AggregateExpression.Count(column, distinct, alias)

fun sum(column: Column<*>, alias: String? = null) =
    AggregateExpression.Sum(column, alias)

fun avg(column: Column<*>, alias: String? = null) =
    AggregateExpression.Avg(column, alias)

fun max(column: Column<*>, alias: String? = null) =
    AggregateExpression.Max(column, alias)

fun min(column: Column<*>, alias: String? = null) =
    AggregateExpression.Min(column, alias)