package com.korm.dsl.dialect

object H2Dialect : Dialect {
    override val name = "H2"

    override fun dataType(type: String): String = when(type) {
        "INT" -> "INT"
        "BIGINT" -> "BIGINT"
        "VARCHAR" -> "VARCHAR"
        "TEXT" -> "VARCHAR"
        "BOOLEAN" -> "BOOLEAN"
        "DOUBLE" -> "DOUBLE"
        "TIMESTAMP" -> "TIMESTAMP"
        "DATE" -> "DATE"
        "BLOB" -> "BLOB"
        else -> type
    }

    override fun autoIncrement() = "AUTO_INCREMENT"
    override fun limit(count: Int, offset: Int) =
        if (offset > 0) "LIMIT $count OFFSET $offset" else "LIMIT $count"
    override fun placeholder(index: Int) = "?"
}