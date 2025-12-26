package com.korm.dsl.schema

import com.korm.dsl.core.Database

abstract class Table(val tableName: String) {
    val columns = mutableListOf<Column<*>>()

    protected fun <T : Any> column(name: String, type: String): Column<T> {
        return Column<T>(name, type, this).also { columns.add(it) }
    }

    fun int(name: String) = column<Int>(name, "INT")
    fun long(name: String) = column<Long>(name, "BIGINT")
    fun varchar(name: String, length: Int = 255) = column<String>(name, "VARCHAR($length)")
    fun text(name: String) = column<String>(name, "TEXT")
    fun bool(name: String) = column<Boolean>(name, "BOOLEAN")
    fun double(name: String) = column<Double>(name, "DOUBLE")
}

fun Table.create(db: Database) {
    val sql = buildString {
        append("CREATE TABLE IF NOT EXISTS $tableName (\n")

        columns.forEachIndexed { index, col ->
            append("  ${col.name} ")

            when {
                // PostgreSQL uses SERIAL/BIGSERIAL as types
                col.autoIncrement && db.dialect.name == "PostgreSQL" -> {
                    append(
                        when (col.type) {
                            "BIGINT" -> "BIGSERIAL"
                            else -> "SERIAL"
                        }
                    )
                }
                // H2 (including PostgreSQL mode)
                col.autoIncrement && db.dialect.name == "H2" -> {
                    append("${db.dialect.dataType(col.type)} GENERATED ALWAYS AS IDENTITY")
                }

                // MySQL and SQLite use type + AUTO_INCREMENT
                col.autoIncrement && db.dialect.autoIncrement().isNotEmpty() -> {
                    append("${db.dialect.dataType(col.type)} ${db.dialect.autoIncrement()}")
                }
                // Normal column without auto-increment
                else -> {
                    append(db.dialect.dataType(col.type))
                }
            }

            if (col.primaryKey) append(" PRIMARY KEY")
            if (!col.nullable) append(" NOT NULL")
            if (col.unique && !col.primaryKey) append(" UNIQUE")
            if (col.defaultValue != null) {
                append(" DEFAULT ")
                append(
                    when (col.defaultValue) {
                        is String -> "'${col.defaultValue}'"
                        else -> col.defaultValue
                    }
                )
            }

            if (index < columns.size - 1) append(",\n")
        }

        append("\n)")
    }

    db.useConnection { conn ->
        conn.createStatement().execute(sql)
    }
}

fun Table.drop(db: Database) {
    db.useConnection { conn ->
        conn.createStatement().execute("DROP TABLE IF EXISTS $tableName")
    }
}
