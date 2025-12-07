// korm-dsl/korm-dsl-migrations/src/main/kotlin/com/korm/dsl/migrations/MigrationHistoryTable.kt

package com.korm.dsl.migrations

/**
 * Migration history table definition.
 */
class MigrationHistoryTable(private val tableName: String) {

    fun createTableSql(): String {
        return """
            CREATE TABLE IF NOT EXISTS $tableName (
                id BIGSERIAL PRIMARY KEY,
                version BIGINT NOT NULL UNIQUE,
                description VARCHAR(500) NOT NULL,
                executed_at BIGINT NOT NULL,
                execution_time_ms BIGINT NOT NULL,
                success BOOLEAN NOT NULL,
                error_message TEXT,
                checksum VARCHAR(32) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
    }

    fun dropTableSql(): String {
        return "DROP TABLE IF EXISTS $tableName CASCADE"
    }

    fun createIndexSql(): List<String> {
        return listOf(
            "CREATE INDEX IF NOT EXISTS idx_${tableName}_version ON $tableName(version)",
            "CREATE INDEX IF NOT EXISTS idx_${tableName}_executed_at ON $tableName(executed_at)"
        )
    }
}
