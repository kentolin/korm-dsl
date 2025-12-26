package com.korm.dsl.query.expressions

import com.korm.dsl.schema.Column

/**
 * CASE expression for conditional logic in queries
 */
sealed class CaseExpression {
    abstract fun toSQL(): String
    abstract val alias: String?

    /**
     * Add an alias to this expression
     */
    fun `as`(alias: String): CaseExpression {
        return AliasedCaseExpression(this, alias)
    }
}

/**
 * CASE expression with alias
 */
internal class AliasedCaseExpression(
    private val expression: CaseExpression,
    override val alias: String
) : CaseExpression() {
    override fun toSQL(): String {
        return "${expression.toSQL()} AS $alias"
    }
}

/**
 * Simple CASE expression: CASE column WHEN value THEN result ...
 */
class SimpleCaseExpression<T : Any> internal constructor(
    private val column: Column<T>,
    private val cases: List<Pair<T, Any>>,
    private val elseValue: Any?,
    override val alias: String? = null
) : CaseExpression() {

    override fun toSQL(): String {
        val whenClauses = cases.joinToString(" ") { (value, result) ->
            val whenVal = formatValue(value)
            val thenVal = formatValue(result)
            "WHEN $whenVal THEN $thenVal"
        }

        val elseClause = elseValue?.let { "ELSE ${formatValue(it)}" } ?: ""

        return "CASE ${column.name} $whenClauses $elseClause END".trim()
    }

    private fun formatValue(value: Any): String {
        return when (value) {
            is String -> "'$value'"
            is Boolean -> if (value) "TRUE" else "FALSE"
            else -> value.toString()
        }
    }
}

/**
 * Searched CASE expression: CASE WHEN condition THEN result ...
 */
class SearchedCaseExpression internal constructor(
    private val conditions: List<Pair<String, Any>>,
    private val elseValue: Any?,
    override val alias: String? = null
) : CaseExpression() {

    override fun toSQL(): String {
        val whenClauses = conditions.joinToString(" ") { (condition, result) ->
            val thenVal = formatValue(result)
            "WHEN $condition THEN $thenVal"
        }

        val elseClause = elseValue?.let { "ELSE ${formatValue(it)}" } ?: ""

        return "CASE $whenClauses $elseClause END".trim()
    }

    private fun formatValue(value: Any): String {
        return when (value) {
            is String -> "'$value'"
            is Boolean -> if (value) "TRUE" else "FALSE"
            else -> value.toString()
        }
    }
}

/**
 * Builder for simple CASE expression
 */
class SimpleCaseBuilder<T : Any>(private val column: Column<T>) {
    private val cases = mutableListOf<Pair<T, Any>>()
    private var elseValue: Any? = null

    /**
     * Add a WHEN clause
     */
    fun <R : Any> whenever(value: T, result: R): SimpleCaseBuilder<T> {
        cases.add(value to result)
        return this
    }

    /**
     * Add ELSE clause
     */
    fun <R : Any> otherwise(value: R): SimpleCaseExpression<T> {
        elseValue = value
        return SimpleCaseExpression(column, cases, elseValue)
    }

    /**
     * Build without ELSE clause
     */
    fun end(): SimpleCaseExpression<T> {
        return SimpleCaseExpression(column, cases, elseValue)
    }
}

/**
 * Builder for searched CASE expression
 */
class SearchedCaseBuilder {
    private val conditions = mutableListOf<Pair<String, Any>>()
    private var elseValue: Any? = null

    /**
     * Add a WHEN clause with condition
     */
    fun <R : Any> whenever(condition: String, result: R): SearchedCaseBuilder {
        conditions.add(condition to result)
        return this
    }

    /**
     * Add a WHEN clause with column condition
     */
    fun <T : Any, R : Any> whenever(column: Column<T>, operator: String, value: T, result: R): SearchedCaseBuilder {
        val condition = when (value) {
            is String -> "${column.name} $operator '$value'"
            else -> "${column.name} $operator $value"
        }
        conditions.add(condition to result)
        return this
    }

    /**
     * Add ELSE clause
     */
    fun <R : Any> otherwise(value: R): SearchedCaseExpression {
        elseValue = value
        return SearchedCaseExpression(conditions, elseValue)
    }

    /**
     * Build without ELSE clause
     */
    fun end(): SearchedCaseExpression {
        return SearchedCaseExpression(conditions, elseValue)
    }
}

/**
 * DSL functions for CASE expressions
 */

/**
 * Simple CASE: CASE column WHEN ...
 */
fun <T : Any> caseWhen(column: Column<T>): SimpleCaseBuilder<T> {
    return SimpleCaseBuilder(column)
}

/**
 * Searched CASE: CASE WHEN condition ...
 */
fun caseWhen(): SearchedCaseBuilder {
    return SearchedCaseBuilder()
}

/**
 * COALESCE function
 */
class CoalesceExpression internal constructor(
    private val values: List<Any>,
    override val alias: String? = null
) : CaseExpression() {
    override fun toSQL(): String {
        val formattedValues = values.joinToString(", ") { value ->
            when (value) {
                is Column<*> -> value.name
                is String -> "'$value'"
                else -> value.toString()
            }
        }
        return "COALESCE($formattedValues)"
    }
}

/**
 * COALESCE(value1, value2, ...)
 */
fun coalesce(vararg values: Any): CoalesceExpression {
    return CoalesceExpression(values.toList())
}

/**
 * NULLIF function
 */
class NullIfExpression internal constructor(
    private val value1: Any,
    private val value2: Any,
    override val alias: String? = null
) : CaseExpression() {
    override fun toSQL(): String {
        val val1 = formatValue(value1)
        val val2 = formatValue(value2)
        return "NULLIF($val1, $val2)"
    }

    private fun formatValue(value: Any): String {
        return when (value) {
            is Column<*> -> value.name
            is String -> "'$value'"
            else -> value.toString()
        }
    }
}

/**
 * NULLIF(value1, value2)
 */
fun nullIf(value1: Any, value2: Any): NullIfExpression {
    return NullIfExpression(value1, value2)
}
