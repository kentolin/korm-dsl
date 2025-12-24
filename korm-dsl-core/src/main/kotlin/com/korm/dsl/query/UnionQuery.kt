package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.core.map
import com.korm.dsl.schema.Table
import java.sql.ResultSet

/**
 * Represents a UNION query combining multiple SELECT queries
 */
class UnionQuery<T : Table> internal constructor(
    private val db: Database,
    private val queries: List<SelectQuery<T>>,
    private val unionType: UnionType
) {
    enum class UnionType {
        UNION,        // UNION (distinct)
        UNION_ALL     // UNION ALL (includes duplicates)
    }

    /**
     * Add another query to the union
     */
    fun union(query: SelectQuery<T>): UnionQuery<T> {
        return UnionQuery(db, queries + query, UnionType.UNION)
    }

    /**
     * Add another query with UNION ALL
     */
    fun unionAll(query: SelectQuery<T>): UnionQuery<T> {
        return UnionQuery(db, queries + query, UnionType.UNION_ALL)
    }

    /**
     * Execute the UNION query
     */
    fun <R> execute(mapper: (ResultSet) -> R): List<R> {
        val sql = buildString {
            queries.forEachIndexed { index, query ->
                if (index > 0) {
                    when (unionType) {
                        UnionType.UNION -> append(" UNION ")
                        UnionType.UNION_ALL -> append(" UNION ALL ")
                    }
                }
                append("(")
                append(query.buildSqlForSubquery())
                append(")")
            }
        }

        // Collect all parameters from all queries
        val allParams = queries.flatMap { it.getParams() }

        return db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                allParams.forEachIndexed { index, value ->
                    stmt.setObject(index + 1, value)
                }
                stmt.executeQuery().map(mapper)
            }
        }
    }
}

/**
 * Create a UNION query - need database reference
 */
fun <T : Table> union(
    db: Database,
    query1: SelectQuery<T>,
    query2: SelectQuery<T>
): UnionQuery<T> {
    return UnionQuery(db, listOf(query1, query2), UnionQuery.UnionType.UNION)
}

/**
 * Create a UNION ALL query
 */
fun <T : Table> unionAll(
    db: Database,
    query1: SelectQuery<T>,
    query2: SelectQuery<T>
): UnionQuery<T> {
    return UnionQuery(db, listOf(query1, query2), UnionQuery.UnionType.UNION_ALL)
}