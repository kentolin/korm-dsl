package com.korm.dsl.query.expressions

import com.korm.dsl.expressions.AggregateExpression
import com.korm.dsl.schema.Column

/**
 * Enhanced aggregate expression with FILTER support
 *
 * Note: FILTER clause is supported in PostgreSQL and H2, but not in MySQL or SQLite.
 * For MySQL/SQLite, use CASE expressions instead.
 */
class FilteredAggregateExpression internal constructor(
    private val aggregate: AggregateExpression,
    private val filterCondition: String
) {
    /**
     * Generate SQL with FILTER clause
     */
    fun toSQL(): String {
        return "${aggregate.toSql()} FILTER (WHERE $filterCondition)"
    }

    /**
     * Add alias
     */
    fun `as`(alias: String): String {
        return "${toSQL()} AS $alias"
    }
}

/**
 * Add FILTER clause to aggregate
 */
fun AggregateExpression.filter(condition: String): FilteredAggregateExpression {
    return FilteredAggregateExpression(this, condition)
}

/**
 * Add FILTER clause with column condition
 */
fun <V : Any> AggregateExpression.filter(
    column: Column<V>,
    operator: String,
    value: V
): FilteredAggregateExpression {
    val condition = when (value) {
        is String -> "${column.name} $operator '$value'"
        else -> "${column.name} $operator $value"
    }
    return FilteredAggregateExpression(this, condition)
}

/**
 * String aggregation functions
 */
object StringAggregates {
    /**
     * STRING_AGG(column, separator) - PostgreSQL
     * GROUP_CONCAT(column SEPARATOR separator) - MySQL
     * LISTAGG(column, separator) WITHIN GROUP (ORDER BY column) - H2
     */
    fun stringAgg(column: Column<String>, separator: String = ","): StringAggExpression {
        return StringAggExpression(column, separator)
    }
}

/**
 * STRING_AGG expression
 */
class StringAggExpression internal constructor(
    private val column: Column<String>,
    private val separator: String,
    private val distinct: Boolean = false,
    private val orderBy: Pair<Column<*>, Boolean>? = null
) {
    /**
     * Generate SQL - database specific
     */
    fun toSQL(dialect: String = "postgresql"): String {
        val distinctClause = if (distinct) "DISTINCT " else ""
        val orderByClause = orderBy?.let { (col, asc) ->
            " ORDER BY ${col.name} ${if (asc) "ASC" else "DESC"}"
        } ?: ""

        return when (dialect.lowercase()) {
            "postgresql" -> {
                "STRING_AGG($distinctClause${column.name}, '$separator'$orderByClause)"
            }
            "mysql" -> {
                "GROUP_CONCAT($distinctClause${column.name}$orderByClause SEPARATOR '$separator')"
            }
            "h2" -> {
                "LISTAGG(${column.name}, '$separator')$orderByClause WITHIN GROUP (ORDER BY ${column.name})"
            }
            else -> {
                // Fallback to PostgreSQL syntax
                "STRING_AGG($distinctClause${column.name}, '$separator'$orderByClause)"
            }
        }
    }

    /**
     * Add DISTINCT
     */
    fun distinct(): StringAggExpression {
        return StringAggExpression(column, separator, true, orderBy)
    }

    /**
     * Add ORDER BY
     */
    fun orderBy(column: Column<*>, ascending: Boolean = true): StringAggExpression {
        return StringAggExpression(this.column, separator, distinct, column to ascending)
    }

    /**
     * Add alias
     */
    fun `as`(alias: String, dialect: String = "postgresql"): String {
        return "${toSQL(dialect)} AS $alias"
    }
}

/**
 * Array aggregation (PostgreSQL)
 */
class ArrayAggExpression<T : Any> internal constructor(
    private val column: Column<T>,
    private val distinct: Boolean = false,
    private val orderBy: Pair<Column<*>, Boolean>? = null
) {
    /**
     * Generate SQL
     */
    fun toSQL(): String {
        val distinctClause = if (distinct) "DISTINCT " else ""
        val orderByClause = orderBy?.let { (col, asc) ->
            " ORDER BY ${col.name} ${if (asc) "ASC" else "DESC"}"
        } ?: ""

        return "ARRAY_AGG($distinctClause${column.name}$orderByClause)"
    }

    /**
     * Add DISTINCT
     */
    fun distinct(): ArrayAggExpression<T> {
        return ArrayAggExpression(column, true, orderBy)
    }

    /**
     * Add ORDER BY
     */
    fun orderBy(column: Column<*>, ascending: Boolean = true): ArrayAggExpression<T> {
        return ArrayAggExpression(this.column, distinct, column to ascending)
    }

    /**
     * Add alias
     */
    fun `as`(alias: String): String {
        return "${toSQL()} AS $alias"
    }
}

/**
 * ARRAY_AGG function
 */
fun <T : Any> arrayAgg(column: Column<T>): ArrayAggExpression<T> {
    return ArrayAggExpression(column)
}

/**
 * JSON aggregation functions (PostgreSQL)
 */
object JsonAggregates {
    /**
     * JSON_AGG(expression)
     */
    fun jsonAgg(expression: String): JsonAggExpression {
        return JsonAggExpression(expression)
    }

    /**
     * JSON_OBJECT_AGG(key, value)
     */
    fun jsonObjectAgg(key: Column<*>, value: Column<*>): JsonObjectAggExpression {
        return JsonObjectAggExpression(key, value)
    }
}

/**
 * JSON_AGG expression
 */
class JsonAggExpression internal constructor(
    private val expression: String,
    private val orderBy: Pair<Column<*>, Boolean>? = null
) {
    fun toSQL(): String {
        val orderByClause = orderBy?.let { (col, asc) ->
            " ORDER BY ${col.name} ${if (asc) "ASC" else "DESC"}"
        } ?: ""

        return "JSON_AGG($expression$orderByClause)"
    }

    fun orderBy(column: Column<*>, ascending: Boolean = true): JsonAggExpression {
        return JsonAggExpression(expression, column to ascending)
    }

    fun `as`(alias: String): String {
        return "${toSQL()} AS $alias"
    }
}

/**
 * JSON_OBJECT_AGG expression
 */
class JsonObjectAggExpression internal constructor(
    private val key: Column<*>,
    private val value: Column<*>
) {
    fun toSQL(): String {
        return "JSON_OBJECT_AGG(${key.name}, ${value.name})"
    }

    fun `as`(alias: String): String {
        return "${toSQL()} AS $alias"
    }
}

/**
 * Statistical aggregate functions
 */
object StatisticalAggregates {
    /**
     * STDDEV(column) - Standard deviation
     */
    fun <T : Any> stddev(column: Column<T>): StatisticalExpression {
        return StatisticalExpression("STDDEV", column)
    }

    /**
     * VARIANCE(column)
     */
    fun <T : Any> variance(column: Column<T>): StatisticalExpression {
        return StatisticalExpression("VARIANCE", column)
    }

    /**
     * PERCENTILE_CONT(fraction) - Continuous percentile
     */
    fun percentileCont(fraction: Double): PercentileExpression {
        return PercentileExpression("PERCENTILE_CONT", fraction)
    }

    /**
     * PERCENTILE_DISC(fraction) - Discrete percentile
     */
    fun percentileDisc(fraction: Double): PercentileExpression {
        return PercentileExpression("PERCENTILE_DISC", fraction)
    }
}

/**
 * Statistical expression
 */
class StatisticalExpression internal constructor(
    private val function: String,
    private val column: Column<*>
) {
    fun toSQL(): String {
        return "$function(${column.name})"
    }

    fun `as`(alias: String): String {
        return "${toSQL()} AS $alias"
    }
}

/**
 * Percentile expression
 */
class PercentileExpression internal constructor(
    private val function: String,
    private val fraction: Double
) {
    private var withinGroup: Column<*>? = null

    /**
     * WITHIN GROUP (ORDER BY column)
     */
    fun withinGroup(column: Column<*>): PercentileExpression {
        this.withinGroup = column
        return this
    }

    fun toSQL(): String {
        val withinGroupClause = withinGroup?.let {
            " WITHIN GROUP (ORDER BY ${it.name})"
        } ?: ""

        return "$function($fraction)$withinGroupClause"
    }

    fun `as`(alias: String): String {
        return "${toSQL()} AS $alias"
    }
}
