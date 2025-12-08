// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/schema/Index.kt

package com.korm.dsl.schema

/**
 * Represents a database index.
 */
class Index(
    val table: Table,
    val columns: List<Column<*>>,
    val unique: Boolean = false,
    val name: String? = null
) {

    /**
     * Get index name.
     */
    fun getIndexName(): String {
        return name ?: buildString {
            append("idx_")
            append(table.tableName)
            append("_")
            append(columns.joinToString("_") { it.name })
        }
    }

    /**
     * Generate CREATE INDEX statement.
     */
    fun toSql(): String {
        val indexName = getIndexName()
        val uniqueKeyword = if (unique) "UNIQUE " else ""
        val columnNames = columns.joinToString(", ") { it.name }

        return "CREATE ${uniqueKeyword}INDEX $indexName ON ${table.tableName} ($columnNames)"
    }
}
