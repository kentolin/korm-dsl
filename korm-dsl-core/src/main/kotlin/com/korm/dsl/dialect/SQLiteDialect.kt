package com.korm.dsl.dialect

object SQLiteDialect : Dialect {
    override val name = "SQLite"

    override fun dataType(type: String): String = when(type) {
        "INT" -> "INTEGER"
        "BIGINT" -> "INTEGER"
        "VARCHAR" -> "TEXT"
        "TEXT" -> "TEXT"
        "BOOLEAN" -> "INTEGER"
        "DOUBLE" -> "REAL"
        "TIMESTAMP" -> "TEXT"
        "DATE" -> "TEXT"
        "BLOB" -> "BLOB"
        else -> type
    }

    override fun autoIncrement() = "AUTOINCREMENT"
    override fun limit(count: Int, offset: Int) =
        if (offset > 0) "LIMIT $count OFFSET $offset" else "LIMIT $count"
    override fun placeholder(index: Int) = "?"
}