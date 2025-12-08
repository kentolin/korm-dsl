// korm-dsl/korm-dsl-core/src/main/kotlin/com/korm/dsl/schema/Table.kt

package com.korm.dsl.schema

import com.korm.dsl.types.*

/**
 * Base class for table definitions using DSL.
 */
abstract class Table(val tableName: String) {
    private val columns = mutableListOf<Column<*>>()

    open val primaryKey: PrimaryKey? = null
    private val foreignKeys = mutableListOf<ForeignKey>()
    private val indices = mutableListOf<Index>()

    /**
     * Define an integer column.
     */
    protected fun int(name: String): Column<Int> {
        return Column(name, IntColumnType()).also { columns.add(it) }
    }

    /**
     * Define a long column.
     */
    protected fun long(name: String): Column<Long> {
        return Column(name, LongColumnType()).also { columns.add(it) }
    }

    /**
     * Define a varchar column.
     */
    protected fun varchar(name: String, length: Int): Column<String> {
        return Column(name, VarcharColumnType(length)).also { columns.add(it) }
    }

    /**
     * Define a text column.
     */
    protected fun text(name: String): Column<String> {
        return Column(name, TextColumnType()).also { columns.add(it) }
    }

    /**
     * Define a boolean column.
     */
    protected fun boolean(name: String): Column<Boolean> {
        return Column(name, BooleanColumnType()).also { columns.add(it) }
    }

    /**
     * Define a timestamp column.
     */
    protected fun timestamp(name: String): Column<Long> {
        return Column(name, TimestampColumnType()).also { columns.add(it) }
    }

    /**
     * Define a decimal column.
     */
    protected fun decimal(name: String, precision: Int, scale: Int): Column<Double> {
        return Column(name, DecimalColumnType(precision, scale)).also { columns.add(it) }
    }

    /**
     * Get all columns in this table.
     */
    fun getColumns(): List<Column<*>> = columns.toList()

    /**
     * Generate CREATE TABLE statement.
     */
    fun createStatement(): String {
        val columnDefs = columns.joinToString(", ") { it.toSql() }
        val pkDef = primaryKey?.let { ", ${it.toSql()}" } ?: ""
        val fkDefs = foreignKeys.joinToString("") { ", ${it.toSql()}" }

        return "CREATE TABLE IF NOT EXISTS $tableName ($columnDefs$pkDef$fkDefs)"
    }

    /**
     * Add a foreign key constraint.
     */
    protected fun foreignKey(
        column: Column<*>,
        referencedTable: Table,
        referencedColumn: Column<*>
    ): ForeignKey {
        return ForeignKey(column, referencedTable, referencedColumn).also {
            foreignKeys.add(it)
        }
    }

    /**
     * Add an index.
     */
    protected fun index(vararg columns: Column<*>, unique: Boolean = false): Index {
        return Index(this, columns.toList(), unique).also {
            indices.add(it)
        }
    }
}
