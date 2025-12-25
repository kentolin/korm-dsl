package com.korm.examples.migration

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.migrations.*
import com.korm.dsl.schema.Table

// Define table schemas for reference
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val username = varchar("username", 50).notNull().unique()
    val email = varchar("email", 255).notNull().unique()
}

object Posts : Table("posts") {
    val id = int("id").primaryKey().autoIncrement()
    val userId = int("user_id").notNull()
    val title = varchar("title", 200).notNull()
    val content = text("content").notNull()
}

object Categories : Table("categories") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull().unique()
    val description = text("description")
}

// Migration 1: Create initial tables
val migration_001_initial_schema = migration(
    version = 1,
    description = "Create users and posts tables"
) {
    up {
        // Create users table
        createTable(Users)

        // Create posts table
        createTable(Posts)

        // Add index on user_id for better query performance
        createIndex("posts", "user_id", "idx_posts_user_id")
    }

    down {
        // Drop tables in reverse order
        dropTable("posts")
        dropTable("users")
    }
}

// Migration 2: Add new columns
val migration_002_add_user_fields = migration(
    version = 2,
    description = "Add bio and avatar fields to users"
) {
    up {
        modifyTable("users") {
            addColumn(varchar("bio", 500))
            addColumn(varchar("avatar_url", 255))
        }
    }

    down {
        modifyTable("users") {
            dropColumn("avatar_url")
            dropColumn("bio")
        }
    }
}

// Migration 3: Add published status to posts
val migration_003_add_post_status = migration(
    version = 3,
    description = "Add published status to posts"
) {
    up {
        modifyTable("posts") {
            addColumn(bool("is_published").default(false))
            addColumn(varchar("published_at", 50))
        }

        // Create index on published status
        createIndex("posts", "is_published", "idx_posts_published")
    }

    down {
        modifyTable("posts") {
            dropIndex("idx_posts_published")
            dropColumn("published_at")
            dropColumn("is_published")
        }
    }
}

// Migration 4: Add created_at timestamps
val migration_004_add_timestamps = migration(
    version = 4,
    description = "Add created_at timestamps to all tables"
) {
    up {
        modifyTable("users") {
            addColumn(varchar("created_at", 50).notNull().default(""))
        }

        modifyTable("posts") {
            addColumn(varchar("created_at", 50).notNull().default(""))
        }
    }

    down {
        modifyTable("posts") {
            dropColumn("created_at")
        }

        modifyTable("users") {
            dropColumn("created_at")
        }
    }
}

// Migration 5: Create categories table
val migration_005_add_categories = migration(
    version = 5,
    description = "Add categories table and link to posts"
) {
    up {
        // Create categories table
        createTable(Categories)

        // Add category_id to posts
        modifyTable("posts") {
            addColumn(int("category_id"))
        }

        // Add index for category lookups
        createIndex("posts", "category_id", "idx_posts_category_id")
    }

    down {
        modifyTable("posts") {
            dropIndex("idx_posts_category_id")
            dropColumn("category_id")
        }

        dropTable("categories")
    }
}

// Migration 6: Raw SQL example - Add view count to posts
val migration_006_add_view_count = migration(
    version = 6,
    description = "Add view count tracking to posts"
) {
    up {
        // Add view_count column
        modifyTable("posts") {
            addColumn(int("view_count").notNull().default(0))
        }

        // Initialize view counts using raw SQL (H2 syntax)
        executeSql("UPDATE posts SET view_count = 0 WHERE view_count IS NULL")
    }

    down {
        modifyTable("posts") {
            dropColumn("view_count")
        }
    }
}

fun main() {
    println("=== KORM DSL Migration Example ===\n")

    // Create H2 in-memory database
    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:migrations_demo;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    val db = Database(H2Dialect, pool)

    try {
        // Create migration manager with all migrations
        val migrations = listOf(
            migration_001_initial_schema,
            migration_002_add_user_fields,
            migration_003_add_post_status,
            migration_004_add_timestamps,
            migration_005_add_categories,
            migration_006_add_view_count
        )

        val manager = MigrationManager(db, migrations)

        // Example 1: Show initial status
        println("--- Initial Status ---")
        manager.printStatus()

        // Example 2: Run all migrations
        println("\n--- Applying All Migrations ---")
        val result = manager.migrate()
        println("Result: Applied ${result.applied} migration(s)\n")

        // Example 3: Show status after migration
        println("--- Status After Migration ---")
        manager.printStatus()

        // Example 4: Rollback one migration
        println("\n--- Rolling Back Last Migration ---")
        manager.rollback(steps = 1)

        // Example 5: Show status after rollback
        println("\n--- Status After Rollback ---")
        manager.printStatus()

        // Example 6: Migrate to specific version
        println("\n--- Migrating to Version 3 ---")
        manager.migrateTo(3)
        manager.printStatus()

        // Example 7: Show applied migrations
        println("\n--- Applied Migrations ---")
        val applied = manager.getAppliedMigrations()
        applied.forEach { migration ->
            println("  Version ${migration.version}: ${migration.description}")
            println("    Applied at: ${migration.appliedAt}")
            println("    Execution time: ${migration.executionTimeMs}ms")
        }

        // Example 8: Show pending migrations
        println("\n--- Pending Migrations ---")
        val pending = manager.getPendingMigrations()
        pending.forEach { migration ->
            println("  Version ${migration.version}: ${migration.description}")
        }

        println("\n✓ Migration examples completed successfully!")

    } catch (e: Exception) {
        println("\n✗ Error: ${e.message}")
        e.printStackTrace()
    } finally {
        db.close()
    }
}
