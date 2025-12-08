// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/query/JoinQuery.kt

package com.korm.dsl.query

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

/**
 * JOIN types.
 */
enum class JoinType(val sql: String) {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN"),
    FULL("FULL OUTER JOIN"),
    CROSS("CROSS JOIN")
}

/**
 * Represents a JOIN operation.
 */
class JoinClause(
    val type: JoinType,
    val table: Table,
    val condition: String
) {
    fun toSql(): String {
        return "${type.sql} ${table.tableName} ON $condition"
    }
}

/**
 * Builder for JOIN queries.
 */
class JoinQueryBuilder(private val baseTable: Table) {
    private val joins = mutableListOf<JoinClause>()

    /**
     * Add INNER JOIN.
     */
    fun innerJoin(table: Table, onColumn: Column<*>, withColumn: Column<*>): JoinQueryBuilder {
        joins.add(
            JoinClause(
                JoinType.INNER,
                table,
                "${onColumn.name} = ${withColumn.name}"
            )
        )
        return this
    }

    /**
     * Add LEFT JOIN.
     */
    fun leftJoin(table: Table, onColumn: Column<*>, withColumn: Column<*>): JoinQueryBuilder {
        joins.add(
            JoinClause(
                JoinType.LEFT,
                table,
                "${onColumn.name} = ${withColumn.name}"
            )
        )
        return this
    }

    /**
     * Add RIGHT JOIN.
     */
    fun rightJoin(table: Table, onColumn: Column<*>, withColumn: Column<*>): JoinQueryBuilder {
        joins.add(
            JoinClause(
                JoinType.RIGHT,
                table,
                "${onColumn.name} = ${withColumn.name}"
            )
        )
        return this
    }

    /**
     * Add FULL OUTER JOIN.
     */
    fun fullJoin(table: Table, onColumn: Column<*>, withColumn: Column<*>): JoinQueryBuilder {
        joins.add(
            JoinClause(
                JoinType.FULL,
                table,
                "${onColumn.name} = ${withColumn.name}"
            )
        )
        return this
    }

    /**
     * Add CROSS JOIN.
     */
    fun crossJoin(table: Table): JoinQueryBuilder {
        joins.add(
            JoinClause(
                JoinType.CROSS,
                table,
                ""
            )
        )
        return this
    }

    /**
     * Get all join clauses.
     */
    fun getJoins(): List<JoinClause> = joins.toList()

    /**
     * Build join SQL.
     */
    fun buildJoinSql(): String {
        return joins.joinToString(" ") { it.toSql() }
    }
}
