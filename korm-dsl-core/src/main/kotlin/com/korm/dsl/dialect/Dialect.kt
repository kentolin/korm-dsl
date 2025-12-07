// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/dialect/Dialect.kt

package com.korm.dsl.dialect

/**
 * Base interface for database-specific SQL dialects.
 */
interface Dialect {
    /**
     * Get the name of this dialect.
     */
    val name: String

    /**
     * Get auto-increment syntax.
     */
    fun autoIncrementSyntax(): String

    /**
     * Get limit/offset syntax.
     */
    fun limitOffsetSyntax(limit: Int?, offset: Int?): String

    /**
     * Check if dialect supports returning clause.
     */
    fun supportsReturning(): Boolean

    /**
     * Get current timestamp function.
     */
    fun currentTimestamp(): String

    /**
     * Get boolean type name.
     */
    fun booleanType(): String
}




