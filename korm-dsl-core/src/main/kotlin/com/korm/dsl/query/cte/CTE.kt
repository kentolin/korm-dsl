package com.korm.dsl.query.cte

import com.korm.dsl.core.Database
import java.sql.ResultSet

/**
 * Common Table Expression (CTE) - WITH clause
 *
 * CTEs allow you to define temporary named result sets that exist within a single query.
 * They improve readability and can help optimize complex queries.
 *
 * Example usage with raw SQL:
 * ```kotlin
 * val sql = """
 *     WITH high_earners AS (
 *         SELECT * FROM employees WHERE salary > 90000
 *     )
 *     SELECT department, COUNT(*) as count
 *     FROM high_earners
 *     GROUP BY department
 * """
 *
 * db.useConnection { conn ->
 *     conn.createStatement().use { stmt ->
 *         val rs = stmt.executeQuery(sql)
 *         while (rs.next()) {
 *             // Process results
 *         }
 *     }
 * }
 * ```
 */
class CTE(
    val name: String,
    val sql: String
) {
    /**
     * Generate SQL for this CTE
     */
    fun toSQL(): String {
        return "$name AS ($sql)"
    }
}

/**
 * Query with CTEs
 */
class CTEQuery(
    private val db: Database,
    private val ctes: List<CTE>,
    private val mainSql: String
) {

    /**
     * Generate complete SQL with CTEs
     */
    fun toSQL(): String {
        val cteSQL = ctes.joinToString(", ") { it.toSQL() }
        return "WITH $cteSQL $mainSql"
    }

    /**
     * Execute the CTE query
     */
    fun <R> execute(mapper: (ResultSet) -> R): List<R> {
        val sql = toSQL()
        val results = mutableListOf<R>()

        db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    results.add(mapper(rs))
                }
            }
        }

        return results
    }

    /**
     * Execute and return single result
     */
    fun <R> executeSingle(mapper: (ResultSet) -> R): R? {
        val results = execute(mapper)
        return results.firstOrNull()
    }
}

/**
 * Builder for CTE queries
 */
class CTEBuilder(private val db: Database) {
    private val ctes = mutableListOf<CTE>()

    /**
     * Add a CTE with SQL string
     */
    fun with(name: String, sql: String): CTEBuilder {
        ctes.add(CTE(name, sql))
        return this
    }

    /**
     * Add final query
     */
    fun select(sql: String): CTEQuery {
        return CTEQuery(db, ctes, sql)
    }
}

/**
 * Create a CTE query
 */
fun Database.withCTE(block: CTEBuilder.() -> CTEQuery): CTEQuery {
    val builder = CTEBuilder(this)
    return builder.block()
}

/**
 * Reference a CTE in a query
 */
class CTEReference(val name: String) {
    /**
     * Generate SQL reference
     */
    fun toSQL(): String = name
}

/**
 * Create a CTE reference
 */
fun cte(name: String): CTEReference = CTEReference(name)
