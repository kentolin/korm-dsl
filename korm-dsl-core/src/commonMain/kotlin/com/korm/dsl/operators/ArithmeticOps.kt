// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/operators/ArithmeticOps.kt

package com.korm.dsl.operators

import com.korm.dsl.expressions.BinaryExpression
import com.korm.dsl.expressions.ColumnExpression
import com.korm.dsl.expressions.Expression
import com.korm.dsl.expressions.LiteralExpression
import com.korm.dsl.schema.Column

/**
 * Arithmetic operators for SQL expressions.
 */

// Addition
infix fun <T : Number> Column<T>.plus(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "+",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

infix fun <T : Number> Column<T>.plus(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "+",
        ColumnExpression(other)
    )
}

// Subtraction
infix fun <T : Number> Column<T>.minus(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "-",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

infix fun <T : Number> Column<T>.minus(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "-",
        ColumnExpression(other)
    )
}

// Multiplication
infix fun <T : Number> Column<T>.times(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "*",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

infix fun <T : Number> Column<T>.times(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "*",
        ColumnExpression(other)
    )
}

// Division
infix fun <T : Number> Column<T>.div(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "/",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

infix fun <T : Number> Column<T>.div(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "/",
        ColumnExpression(other)
    )
}

// Modulo
infix fun <T : Number> Column<T>.mod(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "%",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

infix fun <T : Number> Column<T>.mod(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "%",
        ColumnExpression(other)
    )
}
