package com.korm.dsl.query

import com.korm.dsl.core.Database
import com.korm.dsl.schema.Column
import com.korm.dsl.schema.Table

class DeleteQuery<T : Table>(
    private val table: T,
    private val db: Database
) {
    private val whereClauses = mutableListOf<String>()
    private val params = mutableListOf<Any?>()

    fun where(column: Column<*>, value: Any?): DeleteQuery<T> {
        whereClauses.add("${column.name} = ?")
        params.add(value)
        return this
    }

    fun execute(): Int {
        val sql = buildString {
            append("DELETE FROM ${table.tableName}")
            if (whereClauses.isNotEmpty()) {
                append(" WHERE ")
                append(whereClauses.joinToString(" AND "))
            }
        }

        return db.useConnection { conn ->
            conn.prepareStatement(sql).use { stmt ->
                params.forEachIndexed { index, value ->
                    stmt.setObject(index + 1, value)
                }
                stmt.executeUpdate()
            }
        }
    }
}

fun <T : Table> T.delete(db: Database) = DeleteQuery(this, db)