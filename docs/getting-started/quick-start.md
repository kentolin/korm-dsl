# Quick Start Guide

Get up and running with KORM DSL in 5 minutes.

---

## Installation

### Gradle (Kotlin DSL)

Add the following to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "2.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // KORM DSL Core
    implementation("com.korm:korm-dsl-core:0.1.0")
    
    // Choose your database driver
    implementation("com.h2database:h2:2.3.232")                    // H2
    // OR
    implementation("org.postgresql:postgresql:42.7.4")             // PostgreSQL
    // OR
    implementation("com.mysql:mysql-connector-j:9.1.0")           // MySQL
    // OR
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")             // SQLite
    
    // Logging (optional but recommended)
    implementation("ch.qos.logback:logback-classic:1.5.12")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

### Gradle (Groovy DSL)

```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '2.1.0'
}

repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.korm:korm-dsl-core:0.1.0'
    implementation 'com.h2database:h2:2.3.232'
    implementation 'ch.qos.logback:logback-classic:1.5.12'
}
```

---

## Your First KORM Application

### 1. Define Your Schema

Create a `Tables.kt` file:

```kotlin
package com.example.models

import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val age = int("age")
    val createdAt = varchar("created_at", 50)
}
```

### 2. Create Data Classes (Optional)

```kotlin
package com.example.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int?
)
```

### 3. Set Up Database Connection

```kotlin
package com.example

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.schema.create
import com.korm.dsl.query.*
import com.example.models.Users
import com.example.models.User

fun main() {
    // Create connection pool
    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:mydb;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    
    // Create database instance
    val db = Database(H2Dialect, pool)
    
    try {
        // Your database operations here
        runExample(db)
    } finally {
        // Always close the connection pool
        db.close()
    }
}

fun runExample(db: Database) {
    // We'll add operations here next
}
```

### 4. Perform CRUD Operations

```kotlin
fun runExample(db: Database) {
    // CREATE TABLE
    println("Creating table...")
    Users.create(db)
    
    // INSERT
    println("Inserting users...")
    Users.insert(db)
        .set(Users.name, "Alice")
        .set(Users.email, "alice@example.com")
        .set(Users.age, 30)
        .execute()
    
    Users.insert(db)
        .set(Users.name, "Bob")
        .set(Users.email, "bob@example.com")
        .set(Users.age, 25)
        .execute()
    
    // SELECT
    println("\nAll users:")
    val allUsers = Users.select(db).execute { rs ->
        User(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            age = rs.getObject("age") as? Int
        )
    }
    allUsers.forEach { println("  $it") }
    
    // SELECT with WHERE
    println("\nUsers older than 25:")
    val oldUsers = Users.select(db)
        .whereRaw("age > ?", 25)
        .execute { rs ->
            rs.getString("name") to rs.getInt("age")
        }
    oldUsers.forEach { (name, age) ->
        println("  $name is $age years old")
    }
    
    // UPDATE
    println("\nUpdating Bob's age...")
    Users.update(db)
        .set(Users.age, 26)
        .where(Users.name, "Bob")
        .execute()
    
    // DELETE
    println("\nDeleting Alice...")
    Users.delete(db)
        .where(Users.name, "Alice")
        .execute()
    
    // Final count
    val remaining = Users.select(db).execute { rs -> rs.getInt("id") }.size
    println("\nRemaining users: $remaining")
}
```

---

## Database Configuration

### H2 (In-Memory)

```kotlin
val pool = ConnectionPool.create(
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    driver = "org.h2.Driver"
)
val db = Database(H2Dialect, pool)
```

### H2 (File-based)

```kotlin
val pool = ConnectionPool.create(
    url = "jdbc:h2:./data/mydb",
    driver = "org.h2.Driver"
)
val db = Database(H2Dialect, pool)
```

### PostgreSQL

```kotlin
val pool = ConnectionPool.create(
    url = "jdbc:postgresql://localhost:5432/mydb",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "password",
    maximumPoolSize = 10
)
val db = Database(PostgresDialect, pool)
```

### MySQL

```kotlin
val pool = ConnectionPool.create(
    url = "jdbc:mysql://localhost:3306/mydb",
    driver = "com.mysql.cj.jdbc.Driver",
    user = "root",
    password = "password",
    maximumPoolSize = 10
)
val db = Database(MySQLDialect, pool)
```

### SQLite

```kotlin
val pool = ConnectionPool.create(
    url = "jdbc:sqlite:data.db",
    driver = "org.sqlite.JDBC"
)
val db = Database(SQLiteDialect, pool)
```

---

## Running the Application

### From Command Line

```bash
./gradlew run
```

### From IDE

Run the `main()` function in your IDE (IntelliJ IDEA, VS Code, etc.)

---

## Complete Example

Here's a complete working example:

```kotlin
package com.example

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.schema.Table
import com.korm.dsl.schema.create
import com.korm.dsl.query.*

object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val age = int("age")
}

data class User(val id: Int, val name: String, val email: String, val age: Int?)

fun main() {
    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    
    val db = Database(H2Dialect, pool)
    
    try {
        // Create table
        Users.create(db)
        
        // Insert users
        Users.insert(db)
            .set(Users.name, "Alice")
            .set(Users.email, "alice@example.com")
            .set(Users.age, 30)
            .execute()
        
        Users.insert(db)
            .set(Users.name, "Bob")
            .set(Users.email, "bob@example.com")
            .set(Users.age, 25)
            .execute()
        
        // Query users
        val users = Users.select(db).execute { rs ->
            User(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                age = rs.getObject("age") as? Int
            )
        }
        
        println("Users in database:")
        users.forEach { println("  - ${it.name} (${it.email})") }
        
    } finally {
        db.close()
    }
}
```

---

## Next Steps

- **[Table Definition](../core-concepts/table-definition.md)** - Learn about schema definition
- **[Queries](../core-concepts/queries.md)** - Explore query capabilities
- **[Relationships](../core-concepts/relationships.md)** - Work with JOINs and foreign keys
- **[Transactions](../core-concepts/transactions.md)** - Manage transactions

---

## Common Issues

### "Class not found" errors

Make sure you've added the correct database driver dependency.

### Connection pool errors

Check that your database URL, driver class name, and credentials are correct.

### Auto-increment not working

Different databases use different syntax:
- PostgreSQL: Uses `SERIAL` type (handled automatically)
- MySQL/H2: Uses `AUTO_INCREMENT` keyword
- SQLite: Uses `AUTOINCREMENT` keyword

KORM handles these differences automatically based on the dialect.

---

## Running Examples

KORM includes several example projects:

```bash
# Basic CRUD operations
./gradlew :examples:example-basic:run

# Relationships and JOINs
./gradlew :examples:example-relationships:run

# Aggregations
./gradlew :examples:example-aggregates:run

# Advanced features
./gradlew :examples:example-advanced:run
```
