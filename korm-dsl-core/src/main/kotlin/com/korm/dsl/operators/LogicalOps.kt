// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/operators/LogicalOps.kt

package com.korm.dsl.operators

import com.korm.dsl.expressions.BinaryExpression
import com.korm.dsl.expressions.Expression
import com.korm.dsl.expressions.ParenthesizedExpression

/**
 * Logical operators for combining SQL expressions.
 */

// AND operator
infix fun Expression.and(other: Expression): Expression {
    return BinaryExpression(this, "AND", other)
}

// OR operator
infix fun Expression.or(other: Expression): Expression {
    return BinaryExpression(this, "OR", other)
}

// NOT operator
fun not(expression: Expression): Expression {
    return object : Expression {
        override fun toSql(): String = "NOT ${ParenthesizedExpression(expression).toSql()}"
    }
}

// AND with multiple expressions
fun and(vararg expressions: Expression): Expression {
    require(expressions.isNotEmpty()) { "At least one expression required" }
    return expressions.reduce { acc, expr -> acc and expr }
}

// OR with multiple expressions
fun or(vararg expressions: Expression): Expression {
    require(expressions.isNotEmpty()) { "At least one expression required" }
    return expressions.reduce { acc, expr -> acc or expr }
}

// Parentheses for grouping
fun Expression.parenthesized(): Expression {
    return ParenthesizedExpression(this)
}
