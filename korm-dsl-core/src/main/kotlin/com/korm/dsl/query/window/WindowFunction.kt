package com.korm.dsl.query.window

import com.korm.dsl.schema.Column

/**
 * Base class for window functions
 */
sealed class WindowFunction {
    abstract val alias: String?
    abstract fun toSQL(): String

    /**
     * Add an alias to this window function
     */
    fun `as`(alias: String): WindowFunction {
        return AliasedWindowFunction(this, alias)
    }
}

/**
 * Window function with alias
 */
internal class AliasedWindowFunction(
    private val function: WindowFunction,
    override val alias: String
) : WindowFunction() {
    override fun toSQL(): String {
        return "${function.toSQL()} AS $alias"
    }
}

/**
 * ROW_NUMBER() window function
 */
class RowNumberFunction internal constructor(
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        return "ROW_NUMBER()"
    }
}

/**
 * RANK() window function
 */
class RankFunction internal constructor(
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        return "RANK()"
    }
}

/**
 * DENSE_RANK() window function
 */
class DenseRankFunction internal constructor(
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        return "DENSE_RANK()"
    }
}

/**
 * LAG() window function
 */
class LagFunction<T : Any> internal constructor(
    private val column: Column<T>,
    private val offset: Int = 1,
    private val default: T? = null,
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        val defaultValue = default?.let {
            when (it) {
                is String -> "'$it'"
                else -> it.toString()
            }
        }

        return if (defaultValue != null) {
            "LAG(${column.name}, $offset, $defaultValue)"
        } else {
            "LAG(${column.name}, $offset)"
        }
    }
}

/**
 * LEAD() window function
 */
class LeadFunction<T : Any> internal constructor(
    private val column: Column<T>,
    private val offset: Int = 1,
    private val default: T? = null,
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        val defaultValue = default?.let {
            when (it) {
                is String -> "'$it'"
                else -> it.toString()
            }
        }

        return if (defaultValue != null) {
            "LEAD(${column.name}, $offset, $defaultValue)"
        } else {
            "LEAD(${column.name}, $offset)"
        }
    }
}

/**
 * FIRST_VALUE() window function
 */
class FirstValueFunction<T : Any> internal constructor(
    private val column: Column<T>,
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        return "FIRST_VALUE(${column.name})"
    }
}

/**
 * LAST_VALUE() window function
 */
class LastValueFunction<T : Any> internal constructor(
    private val column: Column<T>,
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        return "LAST_VALUE(${column.name})"
    }
}

/**
 * NTH_VALUE() window function
 */
class NthValueFunction<T : Any> internal constructor(
    private val column: Column<T>,
    private val n: Int,
    override val alias: String? = null
) : WindowFunction() {
    override fun toSQL(): String {
        return "NTH_VALUE(${column.name}, $n)"
    }
}

/**
 * Window specification (OVER clause)
 */
class WindowSpec {
    private var partitionByColumns: List<Column<*>> = emptyList()
    private var orderByColumns: List<Pair<Column<*>, Boolean>> = emptyList()
    private var frameClause: String? = null

    /**
     * PARTITION BY clause
     */
    fun partitionBy(vararg columns: Column<*>): WindowSpec {
        this.partitionByColumns = columns.toList()
        return this
    }

    /**
     * ORDER BY clause
     */
    fun orderBy(column: Column<*>, ascending: Boolean = true): WindowSpec {
        this.orderByColumns = this.orderByColumns + (column to ascending)
        return this
    }

    /**
     * Add frame clause (e.g., "ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW")
     */
    fun frame(clause: String): WindowSpec {
        this.frameClause = clause
        return this
    }

    /**
     * Generate SQL for OVER clause
     */
    fun toSQL(): String {
        val parts = mutableListOf<String>()

        if (partitionByColumns.isNotEmpty()) {
            val columns = partitionByColumns.joinToString(", ") { it.name }
            parts.add("PARTITION BY $columns")
        }

        if (orderByColumns.isNotEmpty()) {
            val columns = orderByColumns.joinToString(", ") { (col, asc) ->
                "${col.name} ${if (asc) "ASC" else "DESC"}"
            }
            parts.add("ORDER BY $columns")
        }

        frameClause?.let { parts.add(it) }

        return if (parts.isEmpty()) {
            "OVER ()"
        } else {
            "OVER (${parts.joinToString(" ")})"
        }
    }
}

/**
 * Window function with OVER clause
 */
class WindowFunctionWithSpec internal constructor(
    private val function: WindowFunction,
    private val spec: WindowSpec
) {
    fun toSQL(): String {
        return "${function.toSQL()} ${spec.toSQL()}"
    }

    val alias: String? = function.alias
}

/**
 * DSL for creating window functions
 */
object WindowFunctions {
    /**
     * ROW_NUMBER() OVER (...)
     */
    fun rowNumber(): RowNumberFunction = RowNumberFunction()

    /**
     * RANK() OVER (...)
     */
    fun rank(): RankFunction = RankFunction()

    /**
     * DENSE_RANK() OVER (...)
     */
    fun denseRank(): DenseRankFunction = DenseRankFunction()

    /**
     * LAG(column, offset, default) OVER (...)
     */
    fun <T : Any> lag(column: Column<T>, offset: Int = 1, default: T? = null): LagFunction<T> {
        return LagFunction(column, offset, default)
    }

    /**
     * LEAD(column, offset, default) OVER (...)
     */
    fun <T : Any> lead(column: Column<T>, offset: Int = 1, default: T? = null): LeadFunction<T> {
        return LeadFunction(column, offset, default)
    }

    /**
     * FIRST_VALUE(column) OVER (...)
     */
    fun <T : Any> firstValue(column: Column<T>): FirstValueFunction<T> {
        return FirstValueFunction(column)
    }

    /**
     * LAST_VALUE(column) OVER (...)
     */
    fun <T : Any> lastValue(column: Column<T>): LastValueFunction<T> {
        return LastValueFunction(column)
    }

    /**
     * NTH_VALUE(column, n) OVER (...)
     */
    fun <T : Any> nthValue(column: Column<T>, n: Int): NthValueFunction<T> {
        return NthValueFunction(column, n)
    }
}

/**
 * Apply window specification to a window function
 */
infix fun WindowFunction.over(spec: WindowSpec): WindowFunctionWithSpec {
    return WindowFunctionWithSpec(this, spec)
}

/**
 * Create a window specification
 */
fun window(block: WindowSpec.() -> Unit): WindowSpec {
    val spec = WindowSpec()
    spec.block()
    return spec
}
