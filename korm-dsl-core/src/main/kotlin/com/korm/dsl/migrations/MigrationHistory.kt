package com.korm.dsl.migrations

import com.korm.dsl.schema.Table

/**
 * Table for tracking migration history
 * Stores information about which migrations have been applied
 */
object MigrationHistory : Table("schema_migrations") {
    val version = long("version").primaryKey()
    val description = varchar("description", 500).notNull()
    val appliedAt = varchar("applied_at", 50).notNull()
    val executionTimeMs = long("execution_time_ms").notNull()
}

/**
 * Data class representing a migration history record
 */
data class MigrationRecord(
    val version: Long,
    val description: String,
    val appliedAt: String,
    val executionTimeMs: Long
) {
    override fun toString(): String {
        return "MigrationRecord(version=$version, description='$description', appliedAt='$appliedAt', executionTime=${executionTimeMs}ms)"
    }
}
