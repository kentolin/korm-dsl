<!-- korm-dsl/docs/getting-started/quick-start.md -->

# Quick Start Guide

This guide will help you get started with KORM DSL in just a few minutes.

## Prerequisites

- JDK 11 or higher
- Gradle 8.0 or higher
- A supported database (PostgreSQL, MySQL, SQLite, or H2)

## Installation

### Using Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.korm:korm-dsl-core:0.1.0")
    implementation("org.postgresql:postgresql:42.7.4") // or your database driver
}
```

### Using Gradle (Groovy)
```groovy
dependencies {
    implementation 'com.korm:korm-dsl-core:0.1.0'
    implementation 'org.postgresql:postgresql:42.7.4' // or your database driver
}
```

### Using Maven
```xml
<dependencies>
    <dependency>
        <groupId>com.korm</groupId>
        <artifactId>korm-dsl-core</artifactId>
        <version>0.1.0</version>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.4</version>
    </dependency>
</dependencies>
```

## Your First KORM Application

### Step 1: Define Your Table

Create a Kotlin object that extends `Table`:
```kotlin
import com.korm.dsl.schema.Table
import com.korm.dsl.schema.PrimaryKey

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).unique().notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    
    override val primaryKey = PrimaryKey(id)
}
```

### Step 2: Connect to Database
```kotlin
import com.korm.dsl.core.Database

val database = Database.connect(
    url = "jdbc:postgresql://localhost:5432/mydb",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "password"
)
```

### Step 3: Create Tables
```kotlin
database.createTables(Users)
```

### Step 4: Perform CRUD Operations

#### Insert
```kotlin
val userId = database.transaction {
    insertInto(Users) {
        it[Users.name] = "John Doe"
        it[Users.email] = "john@example.com"
    }
}
```

#### Select
```kotlin
val users = database.transaction {
    from(Users)
        .where(Users.email eq "john@example.com")
        .select()
}
```

#### Update
```kotlin
database.transaction {
    update(Users) {
        it[Users.name] = "Jane Doe"
    }.where(Users.id eq userId)
}
```

#### Delete
```kotlin
database.transaction {
    deleteFrom(Users) {
        where(Users.id eq userId)
    }
}
```

## Next Steps

- Learn about [Table Definitions](../core-concepts/table-definition.md)
- Explore [Query Building](../core-concepts/queries.md)
- Understand [Transactions](../core-concepts/transactions.md)
- Check out [Complete Examples](../../examples/)

## Troubleshooting

### Connection Issues

If you're having trouble connecting to your database:

1. Verify your JDBC URL is correct
2. Ensure your database server is running
3. Check firewall settings
4. Verify credentials

### Common Errors

**Table already exists:**
```kotlin
// Drop tables before creating
database.dropTables(Users)
database.createTables(Users)
```

**Connection pool exhausted:**
```kotlin
// Increase pool size
Database.connect(
    url = "...",
    driver = "...",
    user = "...",
    password = "...",
    maximumPoolSize = 20
)
```

## Getting Help

- [GitHub Issues](https://github.com/yourusername/korm-dsl/issues)
- [Documentation](../README.md)
- [Examples](../../examples/)
