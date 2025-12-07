// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/schema/Column.kt

package com.korm.dsl.schema

import com.korm.dsl.types.ColumnType

/**
 * Represents a database column with type information.
 */
class Column<T>(
    val name: String,
    val type: ColumnType<T>
) {
    var nullable: Boolean = true
        private set

    var autoIncrement: Boolean = false
        private set

    var defaultValue: T? = null
        private set

    var unique: Boolean = false
        private set

    /**
     * Mark column as NOT NULL.
     */
    fun notNull(): Column<T> {
        nullable = false
        return this
    }

    /**
     * Mark column as auto-increment (for integer types).
     */
    fun autoIncrement(): Column<T> {
        autoIncrement = true
        return this
    }

    /**
     * Set default value for column.
     */
    fun default(value: T): Column<T> {
        defaultValue = value
        return this
    }

    /**
     * Mark column as unique.
     */
    fun unique(): Column<T> {
        unique = true
        return this
    }

    /**
     * Generate SQL definition for this column.
     */
    fun toSql(): String {
        val parts = mutableListOf<String>()
        parts.add(name)
        parts.add(type.sqlType())

        if (!nullable) parts.add("NOT NULL")
        if (autoIncrement) parts.add("AUTO_INCREMENT")
        if (unique) parts.add("UNIQUE")
        if (defaultValue != null) parts.add("DEFAULT ${type.valueToSql(defaultValue!!)}")

        return parts.joinToString(" ")
    }

    /**
     * Create an equality expression.
     */
    infix fun eq(value: T): String {
        return "$name = ${type.valueToSql(value)}"
    }

    /**
     * Create a not-equals expression.
     */
    infix fun neq(value: T): String {
        return "$name != ${type.valueToSql(value)}"
    }

    /**
     * Create a greater-than expression.
     */
    infix fun gt(value: T): String {
        return "$name > ${type.valueToSql(value)}"
    }

    /**
     * Create a less-than expression.
     */
    infix fun lt(value: T): String {
        return "$name < ${type.valueToSql(value)}"
    }

    /**
     * Create a LIKE expression.
     */
    infix fun like(value: String): String {
        return "$name LIKE '$value'"
    }
}
