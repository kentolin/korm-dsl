package com.korm.dsl.query

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

enum class JoinType {
    INNER,
    LEFT,
    RIGHT,
    FULL
}

data class Join(
    val type: JoinType,
    val table: Table,
    val condition: JoinCondition
)

sealed class JoinCondition {
    data class On(val left: Column<*>, val right: Column<*>) : JoinCondition()
}

fun JoinCondition.toSql(): String = when (this) {
    is JoinCondition.On -> "${left.table.tableName}.${left.name} = ${right.table.tableName}.${right.name}"
}

fun JoinType.toSql(): String = when (this) {
    JoinType.INNER -> "INNER JOIN"
    JoinType.LEFT -> "LEFT JOIN"
    JoinType.RIGHT -> "RIGHT JOIN"
    JoinType.FULL -> "FULL OUTER JOIN"
}