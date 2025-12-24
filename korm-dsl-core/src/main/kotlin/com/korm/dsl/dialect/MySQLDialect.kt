package com.korm.dsl.dialect

object MySQLDialect : Dialect {
    override val name = "MySQL"

    override fun dataType(type: String): String = when(type) {
        "INT" -> "INT"
        "BIGINT" -> "BIGINT"
        "VARCHAR" -> "VARCHAR"
        "TEXT" -> "TEXT"
        "BOOLEAN" -> "BOOLEAN"
        "DOUBLE" -> "DOUBLE"
        "TIMESTAMP" -> "DATETIME"
        "DATE" -> "DATE"
        "BLOB" -> "BLOB"
        else -> type
    }

    override fun autoIncrement() = "AUTO_INCREMENT"
    override fun limit(count: Int, offset: Int) =
        if (offset > 0) "LIMIT $offset, $count" else "LIMIT $count"
    override fun placeholder(index: Int) = "?"
}