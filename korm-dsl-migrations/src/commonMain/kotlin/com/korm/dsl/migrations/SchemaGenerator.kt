// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/SchemaGenerator.kt

package com.korm.dsl.migrations

import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import com.korm.dsl.core.Database

/**
 * Generates database schema from table definitions.
 */
class SchemaGenerator(private val database: Database) {

    /**
     * Generate and execute CREATE TABLE statements.
     */
    fun createTables(vararg tables: Table) {
        database.transaction {
            tables.forEach { table ->
                val sql = table.createStatement()
                execute(sql)
            }
        }
    }

    /**
     * Generate and execute DROP TABLE statements.
     */
    fun dropTables(vararg tables: Table) {
        database.transaction {
            tables.reversed().forEach { table ->
                val sql = "DROP TABLE IF EXISTS ${table.tableName} CASCADE"
                execute(sql)
            }
        }
    }

    /**
     * Generate CREATE TABLE SQL without executing.
     */
    fun generateCreateTableSql(table: Table): String {
        return table.createStatement()
    }

    /**
     * Generate DROP TABLE SQL without executing.
     */
    fun generateDropTableSql(table: Table): String {
        return "DROP TABLE IF EXISTS ${table.tableName} CASCADE"
    }

    /**
     * Generate ALTER TABLE ADD COLUMN SQL.
     */
    fun generateAddColumnSql(table: Table, column: Column<*>): String {
        return "ALTER TABLE ${table.tableName} ADD COLUMN ${column.toSql()}"
    }

    /**
     * Generate ALTER TABLE DROP COLUMN SQL.
     */
    fun generateDropColumnSql(table: Table, columnName: String): String {
        return "ALTER TABLE ${table.tableName} DROP COLUMN $columnName"
    }

    /**
     * Generate ALTER TABLE RENAME COLUMN SQL.
     */
    fun generateRenameColumnSql(table: Table, oldName: String, newName: String): String {
        return "ALTER TABLE ${table.tableName} RENAME COLUMN $oldName TO $newName"
    }

    /**
     * Generate CREATE INDEX SQL.
     */
    fun generateCreateIndexSql(
        table: Table,
        indexName: String,
        columns: List<String>,
        unique: Boolean = false
    ): String {
        val uniqueKeyword = if (unique) "UNIQUE " else ""
        val columnList = columns.joinToString(", ")
        return "CREATE ${uniqueKeyword}INDEX $indexName ON ${table.tableName} ($columnList)"
    }

    /**
     * Generate DROP INDEX SQL.
     */
    fun generateDropIndexSql(indexName: String): String {
        return "DROP INDEX IF EXISTS $indexName"
    }

    /**
     * Check if table exists.
     */
    fun tableExists(tableName: String): Boolean {
        return database.transaction {
            val sql = """
                SELECT EXISTS (
                    SELECT FROM information_schema.tables
                    WHERE table_name = '$tableName'
                )
            """.trimIndent()

            var exists = false
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            exists = rs.getBoolean(1)
                        }
                    }
                }
            }
            exists
        }
    }

    /**
     * Get list of all tables in database.
     */
    fun listTables(): List<String> {
        return database.transaction {
            val sql = """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                ORDER BY table_name
            """.trimIndent()

            val tables = mutableListOf<String>()
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            tables.add(rs.getString("table_name"))
                        }
                    }
                }
            }
            tables
        }
    }

    /**
     * Get columns for a table.
     */
    fun getTableColumns(tableName: String): List<ColumnInfo> {
        return database.transaction {
            val sql = """
                SELECT column_name, data_type, is_nullable, column_default
                FROM information_schema.columns
                WHERE table_name = '$tableName'
                ORDER BY ordinal_position
            """.trimIndent()

            val columns = mutableListOf<ColumnInfo>()
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            columns.add(
                                ColumnInfo(
                                    name = rs.getString("column_name"),
                                    type = rs.getString("data_type"),
                                    nullable = rs.getString("is_nullable") == "YES",
                                    defaultValue = rs.getString("column_default")
                                )
                            )
                        }
                    }
                }
            }
            columns
        }
    }
}

/**
 * Column information from database schema.
 */
data class ColumnInfo(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val defaultValue: String?
)
