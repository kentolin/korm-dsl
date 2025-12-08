// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/expressions/FunctionExpression.kt

package com.korm.dsl.expressions

import com.korm.dsl.schema.Column

/**
 * SQL function expressions.
 */

// Aggregate functions
fun count(column: Column<*>): Expression {
    return FunctionExpression("COUNT", listOf(ColumnExpression(column)))
}

fun countDistinct(column: Column<*>): Expression {
    return object : Expression {
        override fun toSql(): String = "COUNT(DISTINCT ${column.name})"
    }
}

fun countAll(): Expression {
    return object : Expression {
        override fun toSql(): String = "COUNT(*)"
    }
}

fun <T> sum(column: Column<T>): Expression {
    return FunctionExpression("SUM", listOf(ColumnExpression(column)))
}

fun <T> avg(column: Column<T>): Expression {
    return FunctionExpression("AVG", listOf(ColumnExpression(column)))
}

fun <T> min(column: Column<T>): Expression {
    return FunctionExpression("MIN", listOf(ColumnExpression(column)))
}

fun <T> max(column: Column<T>): Expression {
    return FunctionExpression("MAX", listOf(ColumnExpression(column)))
}

// String functions
fun upper(column: Column<String>): Expression {
    return FunctionExpression("UPPER", listOf(ColumnExpression(column)))
}

fun lower(column: Column<String>): Expression {
    return FunctionExpression("LOWER", listOf(ColumnExpression(column)))
}

fun trim(column: Column<String>): Expression {
    return FunctionExpression("TRIM", listOf(ColumnExpression(column)))
}

fun length(column: Column<String>): Expression {
    return FunctionExpression("LENGTH", listOf(ColumnExpression(column)))
}

fun substring(column: Column<String>, start: Int, length: Int? = null): Expression {
    return if (length != null) {
        object : Expression {
            override fun toSql(): String = "SUBSTRING(${column.name}, $start, $length)"
        }
    } else {
        object : Expression {
            override fun toSql(): String = "SUBSTRING(${column.name}, $start)"
        }
    }
}

fun concat(vararg columns: Column<String>): Expression {
    return FunctionExpression(
        "CONCAT",
        columns.map { ColumnExpression(it) }
    )
}

// Date/Time functions
fun currentTimestamp(): Expression {
    return object : Expression {
        override fun toSql(): String = "CURRENT_TIMESTAMP"
    }
}

fun currentDate(): Expression {
    return object : Expression {
        override fun toSql(): String = "CURRENT_DATE"
    }
}

fun currentTime(): Expression {
    return object : Expression {
        override fun toSql(): String = "CURRENT_TIME"
    }
}

// Math functions
fun abs(column: Column<out Number>): Expression {
    return FunctionExpression("ABS", listOf(ColumnExpression(column)))
}

fun round(column: Column<out Number>, precision: Int = 0): Expression {
    return object : Expression {
        override fun toSql(): String = "ROUND(${column.name}, $precision)"
    }
}

fun floor(column: Column<out Number>): Expression {
    return FunctionExpression("FLOOR", listOf(ColumnExpression(column)))
}

fun ceil(column: Column<out Number>): Expression {
    return FunctionExpression("CEIL", listOf(ColumnExpression(column)))
}

// Coalesce
fun <T> coalesce(vararg columns: Column<T>): Expression {
    return FunctionExpression(
        "COALESCE",
        columns.map { ColumnExpression(it) }
    )
}

// Cast
fun <T> Column<T>.cast(type: String): Expression {
    return object : Expression {
        override fun toSql(): String = "CAST($name AS $type)"
    }
}
