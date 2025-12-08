package com.korm.dsl.dialect

/**
 * H2 dialect.
 */
class H2Dialect : Dialect {
    override val name = "H2"

    override fun autoIncrementSyntax() = "AUTO_INCREMENT"

    override fun limitOffsetSyntax(limit: Int?, offset: Int?): String {
        return buildString {
            limit?.let { append(" LIMIT $it") }
            offset?.let { append(" OFFSET $it") }
        }
    }

    override fun supportsReturning() = false

    override fun currentTimestamp() = "CURRENT_TIMESTAMP"

    override fun booleanType() = "BOOLEAN"
}
