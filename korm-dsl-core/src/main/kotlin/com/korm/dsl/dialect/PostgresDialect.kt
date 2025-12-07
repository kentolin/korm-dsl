package com.korm.dsl.dialect
/**
 * PostgreSQL dialect.
 */
class PostgresDialect : Dialect {
    override val name = "PostgreSQL"

    override fun autoIncrementSyntax() = "SERIAL"

    override fun limitOffsetSyntax(limit: Int?, offset: Int?): String {
        return buildString {
            limit?.let { append(" LIMIT $it") }
            offset?.let { append(" OFFSET $it") }
        }
    }

    override fun supportsReturning() = true

    override fun currentTimestamp() = "CURRENT_TIMESTAMP"

    override fun booleanType() = "BOOLEAN"
}
