// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/utils/SqlBuilder.kt

package com.korm.dsl.utils

/**
 * Helper class for building SQL statements.
 */
class SqlBuilder {
    private val parts = mutableListOf<String>()

    /**
     * Append a SQL fragment.
     */
    fun append(sql: String): SqlBuilder {
        parts.add(sql)
        return this
    }

    /**
     * Append a SQL fragment conditionally.
     */
    fun appendIf(condition: Boolean, sql: String): SqlBuilder {
        if (condition) {
            parts.add(sql)
        }
        return this
    }

    /**
     * Append a SQL fragment conditionally with a value.
     */
    fun <T> appendIfNotNull(value: T?, sql: (T) -> String): SqlBuilder {
        value?.let { parts.add(sql(it)) }
        return this
    }

    /**
     * Build the final SQL string.
     */
    fun build(separator: String = " "): String {
        return parts.joinToString(separator)
    }

    /**
     * Clear all parts.
     */
    fun clear(): SqlBuilder {
        parts.clear()
        return this
    }

    companion object {
        /**
         * Create a new SQL builder.
         */
        fun create(): SqlBuilder = SqlBuilder()

        /**
         * Build SQL using a DSL.
         */
        fun build(block: SqlBuilder.() -> Unit): String {
            return SqlBuilder().apply(block).build()
        }
    }
}

/**
 * DSL function for building SQL.
 */
fun buildSql(block: SqlBuilder.() -> Unit): String {
    return SqlBuilder.build(block)
}
