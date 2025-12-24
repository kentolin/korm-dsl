package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

class UpdateQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    private val updates = mutableMapOf<String, Any?>()
    private val whereClauses = mutableListOf<String>()
    private val params = mutableListOf<Any?>()

    fun set(column: Column<*>, value: Any?): UpdateQuery<T> {
        updates[column.name] = value
        return this
    }

    fun where(column: Column<*>, value: Any?): UpdateQuery<T> {
        whereClauses.add("${column.name} = ?")
        params.add(value)
        return this
    }

    fun execute(): Int {
        val setClause = updates.keys.joinToString(", ") { "$it = ?" }
        val sql = buildString {
            append("UPDATE ${table.tableName} SET $setClause")
            if (whereClauses.isNotEmpty()) {
                append(" WHERE ")
                append(whereClauses.joinToString(" AND "))
            }
        }

        return db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                var index = 1
                updates.values.forEach { value ->
                    stmt.setObject(index++, value)
                }
                params.forEach { value ->
                    stmt.setObject(index++, value)
                }
                stmt.executeUpdate()
            }
        }
    }
}

fun <T : Table> T.update(db: Database) = UpdateQuery(this, db)