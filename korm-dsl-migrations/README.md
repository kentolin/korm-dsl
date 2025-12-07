# KORM DSL - Migrations Module

Database migration and schema management for KORM DSL.

## Features

- Version-based migrations
- Automatic migration tracking
- Rollback support
- Schema generation
- Flyway integration
- Liquibase integration
- DDL builder
- Migration DSL

## Usage

### Creating Migrations
```kotlin
val migration1 = migration(1, "Create users table") {
    up {
        createTable(Users)
    }
    down {
        dropTable("users")
    }
}

val migration2 = migration(2, "Add email column") {
    up {
        addColumn("users", "email VARCHAR(255) UNIQUE")
    }
    down {
        dropColumn("users", "email")
    }
}
```

### Running Migrations
```kotlin
val database = Database.connect(/* ... */)
val engine = MigrationEngine(database)

val results = engine.migrate(listOf(migration1, migration2))
results.forEach { result ->
    when (result) {
        is MigrationResult.Success -> println("✓ ${result.description}")
        is MigrationResult.Failure -> println("✗ ${result.description}: ${result.error}")
        is MigrationResult.Skipped -> println("- ${result.description}: ${result.reason}")
    }
}
```

### Rollback
```kotlin
engine.rollback(listOf(migration1, migration2), targetVersion = 1)
```

### Using DDL Builder
```kotlin
val statements = ddl {
    createTable(Users)
    addColumn("users", "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    createIndex("idx_users_email", "users", listOf("email"), unique = true)
}

statements.forEach { sql ->
    database.executeRaw(sql)
}
```

## See Also

- [Migration Guide](../../docs/advanced/migrations.md)
- [Examples](../../examples/)
