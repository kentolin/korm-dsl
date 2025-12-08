// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/schema/PrimaryKey.kt

package com.korm.dsl.schema

/**
 * Represents a primary key constraint on one or more columns.
 */
class PrimaryKey(vararg val columns: Column<*>) {

    /**
     * Generate SQL definition for primary key.
     */
    fun toSql(): String {
        val columnNames = columns.joinToString(", ") { it.name }
        return "PRIMARY KEY ($columnNames)"
    }
}
