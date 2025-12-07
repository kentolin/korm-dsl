// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/DDLBuilder.kt

package com.korm.dsl.migrations

import com.korm.dsl.schema.Table

/**
 * Builder for DDL (Data Definition Language) statements.
 */
class DDLBuilder {
    private val statements = mutableListOf<String>()

    /**
     * Create a table.
     */
    fun createTable(table: Table): DDLBuilder {
        statements.add(table.createStatement())
        return this
    }

    /**
     * Drop a table.
     */
    fun dropTable(tableName: String, cascade: Boolean = false): DDLBuilder {
        val cascadeClause = if (cascade) " CASCADE" else ""
        statements.add("DROP TABLE IF EXISTS $tableName$cascadeClause")
        return this
    }

    /**
     * Rename a table.
     */
    fun renameTable(oldName: String, newName: String): DDLBuilder {
        statements.add("ALTER TABLE $oldName RENAME TO $newName")
        return this
    }

    /**
     * Add a column.
     */
    fun addColumn(tableName: String, columnDefinition: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName ADD COLUMN $columnDefinition")
        return this
    }

    /**
     * Drop a column.
     */
    fun dropColumn(tableName: String, columnName: String, cascade: Boolean = false): DDLBuilder {
        val cascadeClause = if (cascade) " CASCADE" else ""
        statements.add("ALTER TABLE $tableName DROP COLUMN $columnName$cascadeClause")
        return this
    }

    /**
     * Rename a column.
     */
    fun renameColumn(tableName: String, oldName: String, newName: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName RENAME COLUMN $oldName TO $newName")
        return this
    }

    /**
     * Modify column type.
     */
    fun alterColumnType(tableName: String, columnName: String, newType: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName ALTER COLUMN $columnName TYPE $newType")
        return this
    }

    /**
     * Set column NOT NULL.
     */
    fun setNotNull(tableName: String, columnName: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName ALTER COLUMN $columnName SET NOT NULL")
        return this
    }

    /**
     * Drop NOT NULL constraint.
     */
    fun dropNotNull(tableName: String, columnName: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName ALTER COLUMN $columnName DROP NOT NULL")
        return this
    }

    /**
     * Set column default.
     */
    fun setDefault(tableName: String, columnName: String, defaultValue: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName ALTER COLUMN $columnName SET DEFAULT $defaultValue")
        return this
    }

    /**
     * Drop column default.
     */
    fun dropDefault(tableName: String, columnName: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName ALTER COLUMN $columnName DROP DEFAULT")
        return this
    }

    /**
     * Add primary key.
     */
    fun addPrimaryKey(tableName: String, constraintName: String, columns: List<String>): DDLBuilder {
        val columnList = columns.joinToString(", ")
        statements.add("ALTER TABLE $tableName ADD CONSTRAINT $constraintName PRIMARY KEY ($columnList)")
        return this
    }

    /**
     * Drop primary key.
     */
    fun dropPrimaryKey(tableName: String, constraintName: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName DROP CONSTRAINT $constraintName")
        return this
    }

    /**
     * Add foreign key.
     */
    fun addForeignKey(
        tableName: String,
        constraintName: String,
        columns: List<String>,
        refTable: String,
        refColumns: List<String>,
        onDelete: String? = null,
        onUpdate: String? = null
    ): DDLBuilder {
        val columnList = columns.joinToString(", ")
        val refColumnList = refColumns.joinToString(", ")

        val sql = buildString {
            append("ALTER TABLE $tableName ADD CONSTRAINT $constraintName ")
            append("FOREIGN KEY ($columnList) REFERENCES $refTable ($refColumnList)")
            onDelete?.let { append(" ON DELETE $it") }
            onUpdate?.let { append(" ON UPDATE $it") }
        }

        statements.add(sql)
        return this
    }

    /**
     * Drop foreign key.
     */
    fun dropForeignKey(tableName: String, constraintName: String): DDLBuilder {
        statements.add("ALTER TABLE $tableName DROP CONSTRAINT $constraintName")
        return this
    }

    /**
     * Create index.
     */
    fun createIndex(
        indexName: String,
        tableName: String,
        columns: List<String>,
        unique: Boolean = false,
        method: String? = null
    ): DDLBuilder {
        val uniqueKeyword = if (unique) "UNIQUE " else ""
        val columnList = columns.joinToString(", ")
        val methodClause = method?.let { " USING $it" } ?: ""

        statements.add("CREATE ${uniqueKeyword}INDEX $indexName ON $tableName$methodClause ($columnList)")
        return this
    }

    /**
     * Drop index.
     */
    fun dropIndex(indexName: String, cascade: Boolean = false): DDLBuilder {
        val cascadeClause = if (cascade) " CASCADE" else ""
        statements.add("DROP INDEX IF EXISTS $indexName$cascadeClause")
        return this
    }

    /**
     * Add unique constraint.
     */
    fun addUniqueConstraint(
        tableName: String,
        constraintName: String,
        columns: List<String>
    ): DDLBuilder {
        val columnList = columns.joinToString(", ")
        statements.add("ALTER TABLE $tableName ADD CONSTRAINT $constraintName UNIQUE ($columnList)")
        return this
    }

    /**
     * Add check constraint.
     */
    fun addCheckConstraint(
        tableName: String,
        constraintName: String,
        condition: String
    ): DDLBuilder {
        statements.add("ALTER TABLE $tableName ADD CONSTRAINT $constraintName CHECK ($condition)")
        return this
    }

    /**
     * Execute raw SQL.
     */
    fun execute(sql: String): DDLBuilder {
        statements.add(sql)
        return this
    }

    /**
     * Build all statements.
     */
    fun build(): List<String> {
        return statements.toList()
    }

    /**
     * Build as single SQL string.
     */
    fun buildSql(separator: String = ";\n"): String {
        return statements.joinToString(separator) + ";"
    }

    /**
     * Clear all statements.
     */
    fun clear(): DDLBuilder {
        statements.clear()
        return this
    }
}

/**
 * DSL for building DDL statements.
 */
fun ddl(block: DDLBuilder.() -> Unit): List<String> {
    return DDLBuilder().apply(block).build()
}
