package com.korm.dsl.dialect

object PostgresDialect : Dialect {
    override val name = "PostgreSQL"

    override fun dataType(type: String): String = when(type) {
        "INT" -> "INTEGER"
        "BIGINT" -> "BIGINT"
        "VARCHAR" -> "VARCHAR"
        "TEXT" -> "TEXT"
        "BOOLEAN" -> "BOOLEAN"
        "DOUBLE" -> "DOUBLE PRECISION"
        "TIMESTAMP" -> "TIMESTAMP"
        "DATE" -> "DATE"
        "BLOB" -> "BYTEA"
        else -> type
    }

    override fun autoIncrement() = ""  // PostgreSQL uses SERIAL type instead
    override fun limit(count: Int, offset: Int) =
        if (offset > 0) "LIMIT $count OFFSET $offset" else "LIMIT $count"
    override fun placeholder(index: Int) = "$$index"
}