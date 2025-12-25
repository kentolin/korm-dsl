package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.dialect.Dialect
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import java.sql.Connection

/**
 * Builder for DDL (Data Definition Language) operations
 * Provides methods for altering database schema
 */
class DDLBuilder(
    private val db: Database,
    private val conn: Connection
) {
    private val dialect: Dialect = db.dialect

    /**
     * Create a table
     */
    fun createTable(table: Table) {
        val sql = buildCreateTableSQL(table)
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Drop a table
     */
    fun dropTable(tableName: String, ifExists: Boolean = true) {
        val sql = if (ifExists) {
            "DROP TABLE IF EXISTS $tableName"
        } else {
            "DROP TABLE $tableName"
        }
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Add a column to an existing table
     */
    fun <T : Any> addColumn(tableName: String, column: Column<T>) {
        val columnDef = buildColumnDefinition(column)
        val sql = "ALTER TABLE $tableName ADD COLUMN $columnDef"
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Drop a column from a table
     */
    fun dropColumn(tableName: String, columnName: String) {
        val sql = "ALTER TABLE $tableName DROP COLUMN $columnName"
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Rename a column
     */
    fun renameColumn(tableName: String, oldName: String, newName: String) {
        val sql = when (dialect) {
            is com.korm.dsl.dialect.PostgresDialect,
            is com.korm.dsl.dialect.H2Dialect,
            is com.korm.dsl.dialect.SQLiteDialect -> {
                "ALTER TABLE $tableName RENAME COLUMN $oldName TO $newName"
            }
            is com.korm.dsl.dialect.MySQLDialect -> {
                // MySQL requires the full column definition
                "ALTER TABLE $tableName CHANGE $oldName $newName"
            }
            else -> throw UnsupportedOperationException("Rename column not supported for this dialect")
        }
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Create an index on a column
     */
    fun createIndex(
        tableName: String,
        columnName: String,
        indexName: String? = null,
        unique: Boolean = false
    ) {
        val name = indexName ?: "idx_${tableName}_${columnName}"
        val uniqueClause = if (unique) "UNIQUE " else ""
        val sql = "CREATE ${uniqueClause}INDEX $name ON $tableName($columnName)"
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Drop an index
     */
    fun dropIndex(indexName: String, tableName: String? = null) {
        val sql = when (dialect) {
            is com.korm.dsl.dialect.MySQLDialect -> {
                require(tableName != null) { "Table name required for MySQL DROP INDEX" }
                "DROP INDEX $indexName ON $tableName"
            }
            else -> {
                "DROP INDEX $indexName"
            }
        }
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Execute raw SQL
     */
    fun executeSql(sql: String) {
        conn.createStatement().use { stmt ->
            stmt.execute(sql)
        }
    }

    /**
     * Execute multiple SQL statements
     */
    fun executeSqlBatch(vararg sqls: String) {
        conn.createStatement().use { stmt ->
            sqls.forEach { sql ->
                stmt.addBatch(sql)
            }
            stmt.executeBatch()
        }
    }

    // Helper methods

    private fun buildCreateTableSQL(table: Table): String {
        val columns = table.columns.joinToString(", ") { column ->
            buildColumnDefinition(column)
        }

        val constraints = mutableListOf<String>()

        // Primary key constraint
        val pkColumns = table.columns.filter { it.primaryKey }
        if (pkColumns.isNotEmpty()) {
            val pkColumnNames = pkColumns.joinToString(", ") { it.name }
            constraints.add("PRIMARY KEY ($pkColumnNames)")
        }

        // Foreign key constraints
        table.columns.filter { it.foreignKey != null }.forEach { column ->
            val fk = column.foreignKey!!
            constraints.add(
                "FOREIGN KEY (${column.name}) REFERENCES ${fk.toTable.tableName}(${fk.toColumn.name})"
            )
        }

        val allDefinitions = if (constraints.isNotEmpty()) {
            "$columns, ${constraints.joinToString(", ")}"
        } else {
            columns
        }

        return "CREATE TABLE IF NOT EXISTS ${table.tableName} ($allDefinitions)"
    }

    private fun <T : Any> buildColumnDefinition(column: Column<T>): String {
        val parts = mutableListOf<String>()

        // Column name and type
        // Handle auto-increment types specially for PostgreSQL
        if (column.autoIncrement && dialect.name == "PostgreSQL") {
            val serialType = when (column.type) {
                "BIGINT" -> "BIGSERIAL"
                else -> "SERIAL"
            }
            parts.add("${column.name} $serialType")
        } else {
            parts.add("${column.name} ${dialect.dataType(column.type)}")

            // Add AUTO_INCREMENT modifier for other databases
            if (column.autoIncrement) {
                val autoIncrementKeyword = dialect.autoIncrement()
                if (autoIncrementKeyword.isNotEmpty()) {
                    parts.add(autoIncrementKeyword)
                }
            }
        }

        // Primary key
        if (column.primaryKey) {
            parts.add("PRIMARY KEY")
        }

        // NOT NULL constraint
        if (!column.nullable) {
            parts.add("NOT NULL")
        }

        // UNIQUE constraint
        if (column.unique) {
            parts.add("UNIQUE")
        }

        // DEFAULT value
        column.defaultValue?.let { defaultVal ->
            val defaultStr = when (defaultVal) {
                is String -> "'$defaultVal'"
                is Boolean -> if (defaultVal) "TRUE" else "FALSE"
                else -> defaultVal.toString()
            }
            parts.add("DEFAULT $defaultStr")
        }

        return parts.joinToString(" ")
    }
}

/**
 * Extension function to get DDL builder for a connection
 */
fun Database.ddl(conn: Connection): DDLBuilder {
    return DDLBuilder(this, conn)
}
