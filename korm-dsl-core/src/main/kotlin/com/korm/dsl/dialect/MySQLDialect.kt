package com.korm.dsl.dialect
/**
 * MySQL dialect.
 */
class MySQLDialect : Dialect {
    override val name = "MySQL"

    override fun autoIncrementSyntax() = "AUTO_INCREMENT"

    override fun limitOffsetSyntax(limit: Int?, offset: Int?): String {
        return when {
            limit != null && offset != null -> " LIMIT $offset, $limit"
            limit != null -> " LIMIT $limit"
            else -> ""
        }
    }

    override fun supportsReturning() = false

    override fun currentTimestamp() = "CURRENT_TIMESTAMP"

    override fun booleanType() = "TINYINT(1)"
}
