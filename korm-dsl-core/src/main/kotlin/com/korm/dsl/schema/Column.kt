package com.korm.dsl.schema

class Column<T : Any>(
    val name: String,
    val type: String,
    val table: Table
) {
    var primaryKey = false
    var autoIncrement = false
    var nullable = true
    var unique = false
    var defaultValue: Any? = null
    var foreignKey: ForeignKey? = null

    fun primaryKey(): Column<T> {
        primaryKey = true
        nullable = false
        return this
    }

    fun autoIncrement(): Column<T> {
        autoIncrement = true
        return this
    }

    fun notNull(): Column<T> {
        nullable = false
        return this
    }

    fun unique(): Column<T> {
        unique = true
        return this
    }

    fun default(value: T): Column<T> {
        defaultValue = value
        return this
    }

    fun references(column: Column<*>): Column<T> {
        foreignKey = ForeignKey(table, this, column.table, column)
        return this
    }
}