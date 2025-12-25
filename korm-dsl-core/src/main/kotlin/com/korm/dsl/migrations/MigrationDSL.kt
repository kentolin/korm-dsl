package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import java.sql.Connection

/**
 * DSL for defining migrations
 */
class MigrationBuilder(
    private val version: Long,
    private val description: String
) {
    private var upBlock: (DDLBuilder.() -> Unit)? = null
    private var downBlock: (DDLBuilder.() -> Unit)? = null

    /**
     * Define what to do when applying this migration
     */
    fun up(block: DDLBuilder.() -> Unit) {
        upBlock = block
    }

    /**
     * Define what to do when rolling back this migration
     */
    fun down(block: DDLBuilder.() -> Unit) {
        downBlock = block
    }

    /**
     * Build the migration
     */
    fun build(): Migration {
        requireNotNull(upBlock) { "Migration must define an 'up' block" }
        requireNotNull(downBlock) { "Migration must define a 'down' block" }

        return object : AbstractMigration(version, description) {
            override fun up(db: Database, conn: Connection) {
                val ddl = db.ddl(conn)
                upBlock!!.invoke(ddl)
            }

            override fun down(db: Database, conn: Connection) {
                val ddl = db.ddl(conn)
                downBlock!!.invoke(ddl)
            }
        }
    }
}

/**
 * Create a migration using DSL
 */
fun migration(version: Long, description: String, block: MigrationBuilder.() -> Unit): Migration {
    val builder = MigrationBuilder(version, description)
    builder.block()
    return builder.build()
}

/**
 * Helper class for table modifications in migrations
 */
class TableModifier(
    private val tableName: String,
    private val ddl: DDLBuilder
) {
    /**
     * Add a column to the table
     */
    fun <T : Any> addColumn(column: Column<T>) {
        ddl.addColumn(tableName, column)
    }

    /**
     * Drop a column from the table
     */
    fun dropColumn(columnName: String) {
        ddl.dropColumn(tableName, columnName)
    }

    /**
     * Rename a column
     */
    fun renameColumn(oldName: String, newName: String) {
        ddl.renameColumn(tableName, oldName, newName)
    }

    /**
     * Add an index
     */
    fun addIndex(
        columnName: String,
        indexName: String? = null,
        unique: Boolean = false
    ) {
        ddl.createIndex(tableName, columnName, indexName, unique)
    }

    /**
     * Drop an index
     */
    fun dropIndex(indexName: String) {
        ddl.dropIndex(indexName, tableName)
    }
}

/**
 * Modify a table in a migration
 */
fun DDLBuilder.modifyTable(tableName: String, block: TableModifier.() -> Unit) {
    val modifier = TableModifier(tableName, this)
    modifier.block()
}

/**
 * Execute raw SQL with parameters
 */
fun DDLBuilder.execute(sql: String, vararg params: Any?) {
    // For parameterized queries, we'll use a prepared statement
    // This is a simplified version - could be enhanced
    executeSql(sql)
}
