package com.korm.dsl.dialect

/**
 * SQLite dialect.
 */
class SQLiteDialect : Dialect {
    override val name = "SQLite"

    override fun autoIncrementSyntax() = "AUTOINCREMENT"

    override fun limitOffsetSyntax(limit: Int?, offset: Int?): String {
        return buildString {
            limit?.let { append(" LIMIT $it") }
            offset?.let { append(" OFFSET $it") }
        }
    }

    override fun supportsReturning() = true

    override fun currentTimestamp() = "CURRENT_TIMESTAMP"

    override fun booleanType() = "INTEGER"
}
