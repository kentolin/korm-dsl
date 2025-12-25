package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import java.sql.Connection

/**
 * Base interface for database migrations
 *
 * Migrations represent schema changes that can be applied (up) or reverted (down)
 */
interface Migration {
    /**
     * Unique version number for this migration
     * Migrations are executed in order based on this version
     */
    val version: Long

    /**
     * Human-readable description of what this migration does
     */
    val description: String

    /**
     * Apply the migration (move forward)
     *
     * @param db Database instance
     * @param conn Connection to use for the migration
     */
    fun up(db: Database, conn: Connection)

    /**
     * Revert the migration (move backward)
     *
     * @param db Database instance
     * @param conn Connection to use for the rollback
     */
    fun down(db: Database, conn: Connection)
}

/**
 * Abstract base class for migrations providing common functionality
 */
abstract class AbstractMigration(
    override val version: Long,
    override val description: String
) : Migration {

    override fun toString(): String {
        return "Migration(version=$version, description='$description')"
    }
}
