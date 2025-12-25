# Schema Migrations

Complete guide to managing database schema changes with KORM's migration system.

---

## Overview

KORM provides a powerful migration system to manage database schema changes over time. Migrations allow you to:

- Version control your database schema
- Apply changes incrementally
- Rollback changes when needed
- Track migration history
- Collaborate with teams on schema changes

---

## Quick Start

### 1. Define Migrations

```kotlin
import com.korm.dsl.migrations.*

val migration_001 = migration(
    version = 1,
    description = "Create users table"
) {
    up {
        createTable(Users)
    }
    
    down {
        dropTable("users")
    }
}
```

### 2. Create Migration Manager

```kotlin
val migrations = listOf(
    migration_001,
    migration_002,
    migration_003
)

val manager = MigrationManager(db, migrations)
```

### 3. Run Migrations

```kotlin
// Apply all pending migrations
manager.migrate()

// Check status
manager.printStatus()
```

---

## Creating Migrations

### Basic Migration Structure

Every migration has:
- **Version**: Unique identifier (typically timestamp or sequential number)
- **Description**: Human-readable explanation
- **Up**: Apply the migration
- **Down**: Revert the migration

```kotlin
val migration_001 = migration(
    version = 20241225_001,
    description = "Create initial schema"
) {
    up {
        // Schema changes to apply
    }
    
    down {
        // How to revert these changes
    }
}
```

### Creating Tables

```kotlin
val createUsersTable = migration(
    version = 1,
    description = "Create users table"
) {
    up {
        createTable(Users)
    }
    
    down {
        dropTable("users")
    }
}
```

### Dropping Tables

```kotlin
val dropLegacyTable = migration(
    version = 2,
    description = "Drop legacy table"
) {
    up {
        dropTable("legacy_users", ifExists = true)
    }
    
    down {
        // Recreate table if needed
        createTable(LegacyUsers)
    }
}
```

---

## Modifying Tables

### Adding Columns

```kotlin
val addEmailColumn = migration(
    version = 3,
    description = "Add email to users"
) {
    up {
        modifyTable("users") {
            addColumn(varchar("email", 255).notNull())
        }
    }
    
    down {
        modifyTable("users") {
            dropColumn("email")
        }
    }
}
```

### Dropping Columns

```kotlin
val removeOldColumn = migration(
    version = 4,
    description = "Remove deprecated field"
) {
    up {
        modifyTable("users") {
            dropColumn("old_field")
        }
    }
    
    down {
        modifyTable("users") {
            addColumn(varchar("old_field", 100))
        }
    }
}
```

### Renaming Columns

```kotlin
val renameColumn = migration(
    version = 5,
    description = "Rename username to login"
) {
    up {
        modifyTable("users") {
            renameColumn("username", "login")
        }
    }
    
    down {
        modifyTable("users") {
            renameColumn("login", "username")
        }
    }
}
```

---

## Indexes

### Creating Indexes

```kotlin
val addEmailIndex = migration(
    version = 6,
    description = "Add index on email"
) {
    up {
        createIndex(
            tableName = "users",
            columnName = "email",
            indexName = "idx_users_email",
            unique = true
        )
    }
    
    down {
        dropIndex("idx_users_email", tableName = "users")
    }
}
```

### Multiple Indexes

```kotlin
val addMultipleIndexes = migration(
    version = 7,
    description = "Add performance indexes"
) {
    up {
        modifyTable("posts") {
            addIndex("user_id", "idx_posts_user_id")
            addIndex("created_at", "idx_posts_created")
            addIndex("status", "idx_posts_status")
        }
    }
    
    down {
        modifyTable("posts") {
            dropIndex("idx_posts_status")
            dropIndex("idx_posts_created")
            dropIndex("idx_posts_user_id")
        }
    }
}
```

---

## Raw SQL

### Execute Raw SQL

For complex operations not covered by the DSL:

```kotlin
val complexMigration = migration(
    version = 8,
    description = "Complex data migration"
) {
    up {
        executeSql("""
            UPDATE users 
            SET status = 'active' 
            WHERE last_login > DATE('now', '-30 days')
        """)
    }
    
    down {
        executeSql("UPDATE users SET status = NULL")
    }
}
```

**⚠️ Important**: When using raw SQL, be aware of database-specific syntax differences:

- **H2**: `AUTO_INCREMENT`, `CURRENT_TIMESTAMP`
- **SQLite**: `AUTOINCREMENT`, `datetime('now')`
- **PostgreSQL**: `SERIAL`, `NOW()`
- **MySQL**: `AUTO_INCREMENT`, `NOW()`

**Best Practice**: Prefer using `createTable()` with Table definitions over raw SQL when possible, as the DDLBuilder automatically generates correct SQL for your database dialect.

```kotlin
// ✅ GOOD - Database-agnostic
val goodMigration = migration(8, "Add categories") {
    up {
        createTable(Categories)  // DDLBuilder handles dialect
    }
    down {
        dropTable("categories")
    }
}

// ⚠️ CAUTION - Database-specific
val cautiousMigration = migration(8, "Add categories") {
    up {
        executeSql("""
            CREATE TABLE categories (
                id INTEGER AUTO_INCREMENT PRIMARY KEY  -- H2 syntax!
            )
        """)
    }
    down {
        dropTable("categories")
    }
}
```
```

### Batch SQL Execution

Execute multiple SQL statements in one call:

```kotlin
val batchUpdate = migration(
    version = 9,
    description = "Cleanup and maintenance"
) {
    up {
        executeSqlBatch(
            "UPDATE users SET verified = false WHERE email IS NULL",
            "UPDATE posts SET view_count = 0 WHERE view_count IS NULL",
            "DELETE FROM sessions WHERE created_at < CURRENT_TIMESTAMP - INTERVAL '90' DAY"
        )
    }
    
    down {
        // Rollback operations if needed
        executeSql("-- Rollback logic here")
    }
}
```

---

## Migration Manager

### Initialize Migration System

```kotlin
val manager = MigrationManager(db, migrations)

// Create migration history table
manager.initialize()
```

### Apply Migrations

```kotlin
// Apply all pending migrations
val result = manager.migrate()
println("Applied ${result.applied} migrations")

// Migrate to specific version
manager.migrateTo(targetVersion = 5)
```

### Rollback Migrations

```kotlin
// Rollback last migration
manager.rollback(steps = 1)

// Rollback last 3 migrations
manager.rollback(steps = 3)

// Rollback to specific version
manager.migrateTo(targetVersion = 2)
```

### Check Migration Status

```kotlin
// Print formatted status
manager.printStatus()

// Get status object
val status = manager.status()
println("Current version: ${status.currentVersion}")
println("Pending: ${status.pendingMigrations.size}")
```

### Get Migration Information

```kotlin
// Get applied migrations
val applied = manager.getAppliedMigrations()
applied.forEach { migration ->
    println("${migration.version}: ${migration.description}")
    println("  Applied at: ${migration.appliedAt}")
    println("  Execution time: ${migration.executionTimeMs}ms")
}

// Get pending migrations
val pending = manager.getPendingMigrations()
pending.forEach { migration ->
    println("${migration.version}: ${migration.description}")
}
```

---

## Migration Versioning

### Sequential Versioning

```kotlin
val migration_001 = migration(1, "Create users") { /* ... */ }
val migration_002 = migration(2, "Add posts") { /* ... */ }
val migration_003 = migration(3, "Add comments") { /* ... */ }
```

### Timestamp Versioning

```kotlin
val migration_20241225_120000 = migration(
    version = 20241225_120000,
    description = "Add user roles"
) { /* ... */ }

val migration_20241225_143000 = migration(
    version = 20241225_143000,
    description = "Add permissions"
) { /* ... */ }
```

### Semantic Versioning

```kotlin
val migration_1_0_0 = migration(100, "Initial release") { /* ... */ }
val migration_1_1_0 = migration(110, "Add feature X") { /* ... */ }
val migration_1_1_1 = migration(111, "Fix schema issue") { /* ... */ }
```

---

## Real-World Examples

### Blog Schema Evolution

```kotlin
// Version 1: Initial schema
val migration_001_initial = migration(1, "Create blog schema") {
    up {
        createTable(Users)
        createTable(Posts)
        createIndex("posts", "user_id", "idx_posts_user")
    }
    down {
        dropTable("posts")
        dropTable("users")
    }
}

// Version 2: Add comments
val migration_002_comments = migration(2, "Add comments") {
    up {
        createTable(Comments)
        createIndex("comments", "post_id", "idx_comments_post")
        createIndex("comments", "user_id", "idx_comments_user")
    }
    down {
        dropTable("comments")
    }
}

// Version 3: Add tags
val migration_003_tags = migration(3, "Add tagging system") {
    up {
        createTable(Tags)
        createTable(PostTags)
        createIndex("post_tags", "post_id", "idx_post_tags_post")
        createIndex("post_tags", "tag_id", "idx_post_tags_tag")
    }
    down {
        dropTable("post_tags")
        dropTable("tags")
    }
}
```

### E-Commerce Schema Evolution

```kotlin
// Version 1: Products
val migration_001_products = migration(1, "Create products") {
    up {
        createTable(Products)
        createTable(Categories)
        createIndex("products", "category_id", "idx_products_category")
    }
    down {
        dropTable("products")
        dropTable("categories")
    }
}

// Version 2: Shopping cart
val migration_002_cart = migration(2, "Add shopping cart") {
    up {
        createTable(CartItems)
        createIndex("cart_items", "user_id", "idx_cart_user")
        createIndex("cart_items", "product_id", "idx_cart_product")
    }
    down {
        dropTable("cart_items")
    }
}

// Version 3: Orders
val migration_003_orders = migration(3, "Add order system") {
    up {
        createTable(Orders)
        createTable(OrderItems)
        createIndex("orders", "user_id", "idx_orders_user")
        createIndex("order_items", "order_id", "idx_order_items_order")
    }
    down {
        dropTable("order_items")
        dropTable("orders")
    }
}
```

---

## Best Practices

### 1. Always Provide Down Migration

```kotlin
// ✅ GOOD - Reversible migration
migration(1, "Add field") {
    up { addColumn("users", varchar("bio", 500)) }
    down { dropColumn("users", "bio") }
}

// ❌ BAD - No rollback strategy
migration(1, "Add field") {
    up { addColumn("users", varchar("bio", 500)) }
    down { /* empty */ }
}
```

### 2. Use Descriptive Names

```kotlin
// ✅ GOOD
migration(1, "Add email verification to users table")

// ❌ BAD
migration(1, "Update users")
```

### 3. One Logical Change Per Migration

```kotlin
// ✅ GOOD - Focused migration
migration(1, "Add user authentication fields") {
    up {
        modifyTable("users") {
            addColumn(varchar("password_hash", 255))
            addColumn(varchar("salt", 255))
            addColumn(bool("email_verified").default(false))
        }
    }
    down { /* ... */ }
}

// ❌ BAD - Mixed concerns
migration(1, "Various updates") {
    up {
        createTable(Posts)
        modifyTable("users") { addColumn(varchar("bio", 500)) }
        dropTable("old_table")
    }
    down { /* ... */ }
}
```

### 4. Test Both Up and Down

```kotlin
@Test
fun `migration should be reversible`() {
    val manager = MigrationManager(db, listOf(migration_001))
    
    // Apply migration
    manager.migrate()
    // Verify changes
    
    // Rollback migration
    manager.rollback()
    // Verify rollback
}
```

### 5. Never Modify Existing Migrations

Once a migration is applied in production:
- ❌ Don't modify it
- ✅ Create a new migration to make further changes

### 6. Backup Before Production Migrations

```bash
# Before running migrations in production
pg_dump mydb > backup_$(date +%Y%m%d_%H%M%S).sql

# Then run migrations
./gradlew runMigrations
```

---

## Common Patterns

### Data Migration

```kotlin
val migrateUserData = migration(10, "Migrate user data") {
    up {
        executeSql("""
            INSERT INTO new_users (id, username, email)
            SELECT id, name, email_address FROM old_users
        """)
    }
    
    down {
        executeSql("DELETE FROM new_users WHERE id IN (SELECT id FROM old_users)")
    }
}
```

### Adding Default Values

```kotlin
val addDefaults = migration(11, "Add default values") {
    up {
        modifyTable("users") {
            addColumn(bool("is_active").default(true))
        }
        
        // Set existing rows
        executeSql("UPDATE users SET is_active = TRUE WHERE is_active IS NULL")
    }
    
    down {
        modifyTable("users") {
            dropColumn("is_active")
        }
    }
}
```

### Renaming Tables

```kotlin
val renameTable = migration(12, "Rename old_users to users") {
    up {
        executeSql("ALTER TABLE old_users RENAME TO users")
    }
    
    down {
        executeSql("ALTER TABLE users RENAME TO old_users")
    }
}
```

---

## Error Handling

### Migration Failure

When a migration fails:
- Transaction is automatically rolled back
- Migration is not recorded in history
- Error message indicates which migration failed

```kotlin
try {
    manager.migrate()
} catch (e: MigrationException) {
    println("Migration failed: ${e.message}")
    // Database is in consistent state (before failed migration)
}
```

### Handling Conflicts

```kotlin
// Check status before migrating
val status = manager.status()
if (status.pendingMigrations.isNotEmpty()) {
    println("Warning: ${status.pendingMigrations.size} pending migrations")
    // Proceed with caution
}
```

---

## Integration with CI/CD

### Running Migrations on Deployment

```kotlin
// src/main/kotlin/RunMigrations.kt
fun main() {
    val db = Database(/* production config */)
    val manager = MigrationManager(db, allMigrations)
    
    println("Current version: ${manager.status().currentVersion}")
    
    val result = manager.migrate()
    
    if (result.isSuccess) {
        println("✓ Migrations completed successfully")
        exitProcess(0)
    } else {
        println("✗ Migrations failed")
        exitProcess(1)
    }
}
```

### Gradle Task

```kotlin
// build.gradle.kts
tasks.register<JavaExec>("runMigrations") {
    group = "application"
    description = "Run database migrations"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("RunMigrationsKt")
}
```

---

## Next Steps

- **[Monitoring](monitoring.md)** (Coming Soon) - Monitor migration execution
- **[Testing](../testing/migrations.md)** (Coming Soon) - Test your migrations
- **[Advanced Patterns](advanced-migrations.md)** (Coming Soon) - Complex scenarios

---

## Troubleshooting

### Migration Not Found

```
Error: Migration 5 not found in migration list
```

**Solution**: Ensure all migrations are included in the manager:
```kotlin
val allMigrations = listOf(
    migration_001,
    migration_002,
    // ... don't forget any!
    migration_005
)
```

### Duplicate Version

```
Error: Duplicate migration versions found: [3]
```

**Solution**: Ensure each migration has a unique version number.

### Down Migration Not Defined

```
Error: Migration must define a 'down' block
```

**Solution**: Always provide both `up` and `down` blocks.
