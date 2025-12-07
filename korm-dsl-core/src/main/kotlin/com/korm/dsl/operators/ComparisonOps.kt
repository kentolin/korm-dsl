// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/operators/ComparisonOps.kt

package com.korm.dsl.operators

import com.korm.dsl.expressions.BinaryExpression
import com.korm.dsl.expressions.ColumnExpression
import com.korm.dsl.expressions.Expression
import com.korm.dsl.expressions.LiteralExpression
import com.korm.dsl.schema.Column

/**
 * Comparison operators for SQL expressions.
 */

// Equals
infix fun <T> Column<T>.eqExpr(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "=",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

// Not equals
infix fun <T> Column<T>.neqExpr(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "!=",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

// Greater than
infix fun <T> Column<T>.gtExpr(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        ">",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

// Greater than or equal
infix fun <T> Column<T>.gteExpr(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        ">=",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

// Less than
infix fun <T> Column<T>.ltExpr(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "<",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

// Less than or equal
infix fun <T> Column<T>.lteExpr(value: T): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "<=",
        LiteralExpression(value) { type.valueToSql(it) }
    )
}

// LIKE
infix fun Column<String>.likeExpr(pattern: String): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "LIKE",
        LiteralExpression(pattern) { "'$it'" }
    )
}

// NOT LIKE
infix fun Column<String>.notLikeExpr(pattern: String): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "NOT LIKE",
        LiteralExpression(pattern) { "'$it'" }
    )
}

// ILIKE (case-insensitive LIKE, PostgreSQL)
infix fun Column<String>.ilikeExpr(pattern: String): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "ILIKE",
        LiteralExpression(pattern) { "'$it'" }
    )
}

// Column to column comparison
infix fun <T> Column<T>.eqColumn(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "=",
        ColumnExpression(other)
    )
}

infix fun <T> Column<T>.neqColumn(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "!=",
        ColumnExpression(other)
    )
}

infix fun <T> Column<T>.gtColumn(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        ">",
        ColumnExpression(other)
    )
}

infix fun <T> Column<T>.ltColumn(other: Column<T>): Expression {
    return BinaryExpression(
        ColumnExpression(this),
        "<",
        ColumnExpression(other)
    )
}
