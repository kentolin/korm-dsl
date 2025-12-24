package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.core.map
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import com.korm.dsl.expressions.AggregateExpression
import java.sql.ResultSet

class SelectQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    private val whereClauses = mutableListOf<String>()
    private val params = mutableListOf<Any?>()
    private var limitValue: Int? = null
    private var offsetValue: Int = 0
    private var orderByClause: String? = null
    private val joins = mutableListOf<Join>()
    private val selectedColumns = mutableListOf<String>()
    private val aggregates = mutableListOf<AggregateExpression>()
    private val groupByColumns = mutableListOf<Column<*>>()
    private val havingClauses = mutableListOf<String>()
    private val havingParams = mutableListOf<Any?>()

    fun select(vararg columns: Column<*>): SelectQuery<T> {
        selectedColumns.clear()
        val nameCount = mutableMapOf<String, Int>()

        columns.forEach { col ->
            val count = nameCount.getOrDefault(col.name, 0)
            nameCount[col.name] = count + 1

            // Add alias if column name appears multiple times
            if (count > 0) {
                selectedColumns.add("${col.table.tableName}.${col.name} AS ${col.table.tableName}_${col.name}")
            } else {
                selectedColumns.add("${col.table.tableName}.${col.name}")
            }
        }
        return this
    }

    fun selectAggregate(vararg aggs: AggregateExpression): SelectQuery<T> {
        aggregates.clear()
        aggregates.addAll(aggs)
        return this
    }

    /**
     * Select specific columns from joined tables along with aggregates
     */
    fun selectWithAggregate(
        vararg columns: Column<*>,
        aggregates: List<AggregateExpression>
    ): SelectQuery<T> {
        selectedColumns.clear()
        columns.forEach { col ->
            selectedColumns.add("${col.table.tableName}.${col.name}")
        }
        this.aggregates.clear()
        this.aggregates.addAll(aggregates)
        return this
    }

    fun groupBy(vararg columns: Column<*>): SelectQuery<T> {
        groupByColumns.clear()
        groupByColumns.addAll(columns)
        return this
    }

    fun having(aggregate: AggregateExpression, operator: String, value: Any): SelectQuery<T> {
        havingClauses.add("${aggregate.toSql().substringBefore(" AS")} $operator ?")
        havingParams.add(value)
        return this
    }

    fun havingRaw(condition: String, vararg values: Any?): SelectQuery<T> {
        havingClauses.add(condition)
        havingParams.addAll(values)
        return this
    }

    fun innerJoin(otherTable: Table, on: (T, Table) -> Pair<Column<*>, Column<*>>): SelectQuery<T> {
        val (left, right) = on(table, otherTable)
        joins.add(Join(JoinType.INNER, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun innerJoinOn(otherTable: Table, left: Column<*>, right: Column<*>): SelectQuery<T> {
        joins.add(Join(JoinType.INNER, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun leftJoin(otherTable: Table, on: (T, Table) -> Pair<Column<*>, Column<*>>): SelectQuery<T> {
        val (left, right) = on(table, otherTable)
        joins.add(Join(JoinType.LEFT, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun leftJoinOn(otherTable: Table, left: Column<*>, right: Column<*>): SelectQuery<T> {
        joins.add(Join(JoinType.LEFT, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun rightJoin(otherTable: Table, on: (T, Table) -> Pair<Column<*>, Column<*>>): SelectQuery<T> {
        val (left, right) = on(table, otherTable)
        joins.add(Join(JoinType.RIGHT, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun rightJoinOn(otherTable: Table, left: Column<*>, right: Column<*>): SelectQuery<T> {
        joins.add(Join(JoinType.RIGHT, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun fullJoin(otherTable: Table, on: (T, Table) -> Pair<Column<*>, Column<*>>): SelectQuery<T> {
        val (left, right) = on(table, otherTable)
        joins.add(Join(JoinType.FULL, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun fullJoinOn(otherTable: Table, left: Column<*>, right: Column<*>): SelectQuery<T> {
        joins.add(Join(JoinType.FULL, otherTable, JoinCondition.On(left, right)))
        return this
    }

    fun where(column: Column<*>, value: Any?): SelectQuery<T> {
        whereClauses.add("${column.table.tableName}.${column.name} = ?")
        params.add(value)
        return this
    }

    fun whereRaw(condition: String, vararg values: Any?): SelectQuery<T> {
        whereClauses.add(condition)
        params.addAll(values)
        return this
    }

    fun limit(count: Int): SelectQuery<T> {
        limitValue = count
        return this
    }

    fun offset(count: Int): SelectQuery<T> {
        offsetValue = count
        return this
    }

    fun orderBy(column: Column<*>, asc: Boolean = true): SelectQuery<T> {
        orderByClause = "${column.table.tableName}.${column.name} ${if (asc) "ASC" else "DESC"}"
        return this
    }

    /**
     * Build SQL for use in a subquery
     */
    internal fun buildSqlForSubquery(): String {
        return buildString {
            // SELECT clause for subquery
            if (selectedColumns.isNotEmpty()) {
                append("SELECT ${selectedColumns.joinToString(", ")} FROM ${table.tableName}")
            } else if (aggregates.isNotEmpty()) {
                val selectParts = mutableListOf<String>()
                groupByColumns.forEach { col ->
                    selectParts.add("${col.table.tableName}.${col.name}")
                }
                aggregates.forEach { agg ->
                    selectParts.add(agg.toSql())
                }
                append("SELECT ${selectParts.joinToString(", ")} FROM ${table.tableName}")
            } else {
                append("SELECT ${table.tableName}.* FROM ${table.tableName}")
            }

            // JOIN clauses
            joins.forEach { join ->
                append(" ${join.type.toSql()} ${join.table.tableName}")
                append(" ON ${join.condition.toSql()}")
            }

            // WHERE clause
            if (whereClauses.isNotEmpty()) {
                append(" WHERE ")
                append(whereClauses.joinToString(" AND "))
            }

            // GROUP BY
            if (groupByColumns.isNotEmpty()) {
                append(" GROUP BY ")
                append(groupByColumns.joinToString(", ") { "${it.table.tableName}.${it.name}" })
            }

            // HAVING
            if (havingClauses.isNotEmpty()) {
                append(" HAVING ")
                append(havingClauses.joinToString(" AND "))
            }

            // ORDER BY
            orderByClause?.let { append(" ORDER BY $it") }

            // LIMIT
            limitValue?.let { append(" ${db.dialect.limit(it, offsetValue)}") }
        }
    }

    /**
     * Get WHERE parameters for subquery
     */
    internal fun getParams(): List<Any?> {
        return params + havingParams
    }

    /**
     * Execute the SELECT query
     */
    fun <R> execute(mapper: (ResultSet) -> R): List<R> {
        val sql = buildString {
            // SELECT clause
            when {
                aggregates.isNotEmpty() -> {
                    // Aggregate query
                    val selectParts = mutableListOf<String>()

                    // Add GROUP BY columns first
                    groupByColumns.forEach { col ->
                        selectParts.add("${col.table.tableName}.${col.name}")
                    }

                    // Add aggregate expressions
                    aggregates.forEach { agg ->
                        selectParts.add(agg.toSql())
                    }

                    append("SELECT ${selectParts.joinToString(", ")} FROM ${table.tableName}")
                }
                selectedColumns.isNotEmpty() -> {
                    append("SELECT ${selectedColumns.joinToString(", ")} FROM ${table.tableName}")
                }
                joins.isEmpty() -> {
                    append("SELECT ${table.tableName}.* FROM ${table.tableName}")
                }
                else -> {
                    // When there are joins, select all columns from base table
                    append("SELECT ${table.tableName}.* ")
                    // Add all columns from joined tables
                    joins.forEach { join ->
                        append(", ${join.table.tableName}.*")
                    }
                    append(" FROM ${table.tableName}")
                }
            }

            // JOIN clauses
            joins.forEach { join ->
                append(" ${join.type.toSql()} ${join.table.tableName}")
                append(" ON ${join.condition.toSql()}")
            }

            // WHERE clause
            if (whereClauses.isNotEmpty()) {
                append(" WHERE ")
                append(whereClauses.joinToString(" AND "))
            }

            // GROUP BY clause
            if (groupByColumns.isNotEmpty()) {
                append(" GROUP BY ")
                append(groupByColumns.joinToString(", ") { "${it.table.tableName}.${it.name}" })
            }

            // HAVING clause
            if (havingClauses.isNotEmpty()) {
                append(" HAVING ")
                append(havingClauses.joinToString(" AND "))
            }

            // ORDER BY clause
            orderByClause?.let { append(" ORDER BY $it") }

            // LIMIT clause
            limitValue?.let { append(" ${db.dialect.limit(it, offsetValue)}") }
        }

        return db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                var paramIndex = 1

                // Set WHERE parameters
                params.forEach { value ->
                    stmt.setObject(paramIndex++, value)
                }

                // Set HAVING parameters
                havingParams.forEach { value ->
                    stmt.setObject(paramIndex++, value)
                }

                stmt.executeQuery().map(mapper)
            }
        }
    }

    fun executeRaw(): List<Map<String, Any?>> {
        return execute { rs ->
            val meta = rs.metaData
            val map = mutableMapOf<String, Any?>()
            for (i in 1..meta.columnCount) {
                map[meta.getColumnName(i)] = rs.getObject(i)
            }
            map
        }
    }
}

fun <T : Table> T.select(db: Database) = SelectQuery(this, db)