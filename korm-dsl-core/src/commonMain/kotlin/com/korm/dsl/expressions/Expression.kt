// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/expressions/Expression.kt

package com.korm.dsl.expressions

import com.korm.dsl.schema.Column

/**
 * Base interface for SQL expressions.
 */
interface Expression {
    /**
     * Convert expression to SQL string.
     */
    fun toSql(): String
}




/**
 * Binary operation expression.
 */
class BinaryExpression(
    private val left: Expression,
    private val operator: String,
    private val right: Expression
) : Expression {
    override fun toSql(): String = "${left.toSql()} $operator ${right.toSql()}"
}

/**
 * Function call expression.
 */
class FunctionExpression(
    private val functionName: String,
    private val arguments: List<Expression>
) : Expression {
    override fun toSql(): String {
        val args = arguments.joinToString(", ") { it.toSql() }
        return "$functionName($args)"
    }
}

/**
 * Parenthesized expression.
 */
class ParenthesizedExpression(private val inner: Expression) : Expression {
    override fun toSql(): String = "(${inner.toSql()})"
}

/**
 * CASE expression.
 */
class CaseExpression : Expression {
    private val whenClauses = mutableListOf<Pair<Expression, Expression>>()
    private var elseClause: Expression? = null

    fun `when`(condition: Expression, result: Expression): CaseExpression {
        whenClauses.add(condition to result)
        return this
    }

    fun `else`(result: Expression): CaseExpression {
        elseClause = result
        return this
    }

    override fun toSql(): String = buildString {
        append("CASE")
        whenClauses.forEach { (condition, result) ->
            append(" WHEN ${condition.toSql()} THEN ${result.toSql()}")
        }
        elseClause?.let { append(" ELSE ${it.toSql()}") }
        append(" END")
    }
}

/**
 * IN expression.
 */
class InExpression<T>(
    private val column: Column<T>,
    private val values: List<T>
) : Expression {
    override fun toSql(): String {
        val valueList = values.joinToString(", ") { column.type.valueToSql(it) }
        return "${column.name} IN ($valueList)"
    }
}

/**
 * BETWEEN expression.
 */
class BetweenExpression<T>(
    private val column: Column<T>,
    private val lower: T,
    private val upper: T
) : Expression {
    override fun toSql(): String {
        return "${column.name} BETWEEN ${column.type.valueToSql(lower)} AND ${column.type.valueToSql(upper)}"
    }
}

/**
 * IS NULL expression.
 */
class IsNullExpression(private val column: Column<*>) : Expression {
    override fun toSql(): String = "${column.name} IS NULL"
}

/**
 * IS NOT NULL expression.
 */
class IsNotNullExpression(private val column: Column<*>) : Expression {
    override fun toSql(): String = "${column.name} IS NOT NULL"
}

// Extension functions for creating expressions

infix fun <T> Column<T>.`in`(values: List<T>): Expression {
    return InExpression(this, values)
}

infix fun <T> Column<T>.between(range: Pair<T, T>): Expression {
    return BetweenExpression(this, range.first, range.second)
}

fun Column<*>.isNull(): Expression {
    return IsNullExpression(this)
}

fun Column<*>.isNotNull(): Expression {
    return IsNotNullExpression(this)
}
