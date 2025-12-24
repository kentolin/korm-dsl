package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.core.map
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table
import java.sql.ResultSet

class InsertQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    private val values = mutableMapOf<String, Any?>()

    fun set(column: Column<*>, value: Any?): InsertQuery<T> {
        values[column.name] = value
        return this
    }

    fun execute(): Int {
        val columnNames = values.keys.joinToString(", ")
        val placeholders = values.keys.joinToString(", ") { "?" }
        val sql = "INSERT INTO ${table.tableName} ($columnNames) VALUES ($placeholders)"

        return db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                values.values.forEachIndexed { index, value ->
                    stmt.setObject(index + 1, value)
                }
                stmt.executeUpdate()
            }
        }
    }
}

fun <T : Table> T.insert(db: Database) = InsertQuery(this, db)