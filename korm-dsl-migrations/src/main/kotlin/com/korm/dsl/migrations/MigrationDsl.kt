// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/MigrationDsl.kt

package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Table

/**
 * DSL for creating migrations.
 */
class MigrationBuilder(
    override val version: Long,
    override val description: String
) : Migration {

    private val upStatements = mutableListOf<(Database) -> Unit>()
    private val downStatements = mutableListOf<(Database) -> Unit>()

    /**
     * Define upgrade actions.
     */
    fun up(block: MigrationContext.() -> Unit) {
        upStatements.add { database ->
            MigrationContext(database).block()
        }
    }

    /**
     * Define downgrade actions.
     */
    fun down(block: MigrationContext.() -> Unit) {
        downStatements.add { database ->
            MigrationContext(database).block()
        }
    }

    override fun up(database: Database) {
        upStatements.forEach { it(database) }
    }

    override fun down(database: Database) {
        downStatements.forEach { it(database) }
    }
}

/**
 * Context for migration operations.
 */
class MigrationContext(private val database: Database) {

    /**
     * Create a table.
     */
    fun createTable(table: Table) {
        database.transaction {
            execute(table.createStatement())
        }
    }

    /**
     * Drop a table.
     */
    fun dropTable(tableName: String) {
        database.transaction {
            execute("DROP TABLE IF EXISTS $tableName CASCADE")
        }
    }

    /**
     * Execute raw SQL.
     */
    fun sql(statement: String) {
        database.transaction {
            execute(statement)
        }
    }

    /**
     * Execute multiple SQL statements.
     */
    fun sql(vararg statements: String) {
        database.transaction {
            statements.forEach { execute(it) }
        }
    }

    /**
     * Add column to table.
     */
    fun addColumn(tableName: String, columnDefinition: String) {
        database.transaction {
            execute("ALTER TABLE $tableName ADD COLUMN $columnDefinition")
        }
    }

    /**
     * Drop column from table.
     */
    fun dropColumn(tableName: String, columnName: String) {
        database.transaction {
            execute("ALTER TABLE $tableName DROP COLUMN $columnName")
        }
    }

    /**
     * Rename table.
     */
    fun renameTable(oldName: String, newName: String) {
        database.transaction {
            execute("ALTER TABLE $oldName RENAME TO $newName")
        }
    }

    /**
     * Rename column.
     */
    fun renameColumn(tableName: String, oldName: String, newName: String) {
        database.transaction {
            execute("ALTER TABLE $tableName RENAME COLUMN $oldName TO $newName")
        }
    }

    /**
     * Create index.
     */
    fun createIndex(indexName: String, tableName: String, columns: List<String>, unique: Boolean = false) {
        val uniqueKeyword = if (unique) "UNIQUE " else ""
        val columnList = columns.joinToString(", ")
        database.transaction {
            execute("CREATE ${uniqueKeyword}INDEX $indexName ON $tableName ($columnList)")
        }
    }

    /**
     * Drop index.
     */
    fun dropIndex(indexName: String) {
        database.transaction {
            execute("DROP INDEX IF EXISTS $indexName")
        }
    }
}

/**
 * Create a migration using DSL.
 */
fun migration(version: Long, description: String, block: MigrationBuilder.() -> Unit): Migration {
    return MigrationBuilder(version, description).apply(block)
}
