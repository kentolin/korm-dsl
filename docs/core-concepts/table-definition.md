# Table Definition

Learn how to define database schemas using KORM DSL's type-safe table definitions.

---

## Basic Table Definition

Tables in KORM are defined as Kotlin objects extending the `Table` class:

```kotlin
import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val age = int("age")
}
```

---

## Column Types

### Numeric Types

```kotlin
object Products : Table("products") {
    val id = int("id")                    // INT
    val userId = long("user_id")          // BIGINT
    val price = double("price")           // DOUBLE/DOUBLE PRECISION
    val quantity = int("quantity")        // INT
}
```

### String Types

```kotlin
object Articles : Table("articles") {
    val title = varchar("title", 200)     // VARCHAR(200)
    val slug = varchar("slug", 100)       // VARCHAR(100)
    val content = text("content")         // TEXT
    val summary = varchar("summary", 500) // VARCHAR(500)
}
```

### Boolean Type

```kotlin
object Settings : Table("settings") {
    val isEnabled = bool("is_enabled")    // BOOLEAN
    val isPublic = bool("is_public")      // BOOLEAN
}
```

---

## Column Constraints

### Primary Key

```kotlin
object Users : Table("users") {
    // Single column primary key
    val id = int("id").primaryKey()
}
```

### Auto-Increment

```kotlin
object Users : Table("users") {
    // Auto-incrementing primary key
    val id = int("id").primaryKey().autoIncrement()
    
    // Or for BIGINT
    val id = long("id").primaryKey().autoIncrement()
}
```

**Note:** KORM handles database-specific auto-increment syntax:
- PostgreSQL: Uses `SERIAL`/`BIGSERIAL` types
- MySQL/H2: Uses `AUTO_INCREMENT` keyword
- SQLite: Uses `AUTOINCREMENT` keyword

### Not Null

```kotlin
object Users : Table("users") {
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull()
    
    // Nullable column (default)
    val middleName = varchar("middle_name", 100)
}
```

### Unique Constraint

```kotlin
object Users : Table("users") {
    val email = varchar("email", 255).notNull().unique()
    val username = varchar("username", 50).notNull().unique()
}
```

### Default Values

```kotlin
object Posts : Table("posts") {
    val views = int("views").default(0)
    val status = varchar("status", 20).default("draft")
    val isPublished = bool("is_published").default(false)
}
```

---

## Foreign Keys

Define relationships between tables using foreign key references:

```kotlin
object Authors : Table("authors") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
}

object Books : Table("books") {
    val id = int("id").primaryKey().autoIncrement()
    val title = varchar("title", 200).notNull()
    
    // Foreign key reference
    val authorId = int("author_id").notNull().references(Authors.id)
}
```

---

## Complete Example

Here's a comprehensive example showing all features:

```kotlin
package com.example.models

import com.korm.dsl.schema.Table

// Authors table
object Authors : Table("authors") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val bio = text("bio")
    val country = varchar("country", 100)
    val isActive = bool("is_active").default(true)
}

// Books table with foreign key
object Books : Table("books") {
    val id = int("id").primaryKey().autoIncrement()
    val title = varchar("title", 200).notNull()
    val isbn = varchar("isbn", 20).notNull().unique()
    val authorId = int("author_id").notNull().references(Authors.id)
    val publishYear = int("publish_year")
    val price = double("price").notNull()
    val pageCount = int("page_count")
    val description = text("description")
}

// Categories table
object Categories : Table("categories") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull().unique()
    val description = text("description")
}

// Junction table for many-to-many relationship
object BookCategories : Table("book_categories") {
    val bookId = int("book_id").notNull().references(Books.id)
    val categoryId = int("category_id").notNull().references(Categories.id)
}
```

---

## Creating Tables

### Basic Table Creation

```kotlin
import com.korm.dsl.schema.create

// Create a single table
Users.create(db)
```

### Creating Multiple Tables

```kotlin
// Create tables in order (respecting foreign key constraints)
Authors.create(db)
Books.create(db)
Categories.create(db)
BookCategories.create(db)
```

### Generated SQL

KORM generates the appropriate SQL for each database:

**H2/MySQL:**
```sql
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY NOT NULL,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  age INT
)
```

**PostgreSQL:**
```sql
CREATE TABLE IF NOT EXISTS users (
  id SERIAL PRIMARY KEY NOT NULL,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  age INTEGER
)
```

---

## Dropping Tables

```kotlin
import com.korm.dsl.schema.drop

// Drop a single table
Users.drop(db)

// Drop multiple tables (reverse order for foreign keys)
BookCategories.drop(db)
Categories.drop(db)
Books.drop(db)
Authors.drop(db)
```

---

## Working with Multiple Databases

Define different dialects for different databases:

```kotlin
// Development (H2)
val devDb = Database(H2Dialect, devPool)

// Production (PostgreSQL)
val prodDb = Database(PostgresDialect, prodPool)

// Testing (SQLite)
val testDb = Database(SQLiteDialect, testPool)

// Same table definition works with all!
Users.create(devDb)
Users.create(prodDb)
Users.create(testDb)
```

---

## Best Practices

### 1. Use Object Declarations

```kotlin
// ✅ GOOD - Object declaration (singleton)
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
}

// ❌ BAD - Class (creates multiple instances)
class Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
}
```

### 2. Consistent Naming

```kotlin
// Table names: lowercase, plural
object Users : Table("users")           // ✅
object OrderItems : Table("order_items") // ✅

// Column names: snake_case
val createdAt = varchar("created_at", 50)    // ✅
val firstName = varchar("first_name", 100)   // ✅
```

### 3. Always Use Primary Keys

```kotlin
// ✅ GOOD
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    // ... other columns
}

// ❌ BAD - No primary key
object Logs : Table("logs") {
    val message = text("message")
    // Missing primary key!
}
```

### 4. Foreign Key References

```kotlin
// ✅ GOOD - Explicit reference
object Books : Table("books") {
    val authorId = int("author_id").notNull().references(Authors.id)
}

// ⚠️ OKAY - But no database-level constraint
object Books : Table("books") {
    val authorId = int("author_id").notNull()
}
```

### 5. Organize by Module

```
src/main/kotlin/
└── com/example/
    └── models/
        ├── Tables.kt          # All table definitions
        ├── User.kt            # User data class
        ├── Book.kt            # Book data class
        └── Category.kt        # Category data class
```

---

## Advanced Features

### Composite Keys (Future Feature)

```kotlin
// Coming soon: Support for composite primary keys
object UserRoles : Table("user_roles") {
    val userId = int("user_id").references(Users.id)
    val roleId = int("role_id").references(Roles.id)
    
    // Future: compositePrimaryKey(userId, roleId)
}
```

### Indexes (Future Feature)

```kotlin
// Coming soon: Index support
object Users : Table("users") {
    val email = varchar("email", 255).notNull().unique()
    
    // Future: index(email)
    // Future: uniqueIndex(email, username)
}
```

---

## Next Steps

- **[Queries](queries.md)** - Learn how to query your tables
- **[Relationships](relationships.md)** - Work with JOINs and foreign keys
- **[Transactions](transactions.md)** - Manage database transactions

---

## Common Patterns

### User Management System

```kotlin
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val username = varchar("username", 50).notNull().unique()
    val email = varchar("email", 255).notNull().unique()
    val passwordHash = varchar("password_hash", 255).notNull()
    val isActive = bool("is_active").default(true)
    val createdAt = varchar("created_at", 50).notNull()
}

object UserProfiles : Table("user_profiles") {
    val userId = int("user_id").primaryKey().references(Users.id)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val bio = text("bio")
    val avatarUrl = varchar("avatar_url", 255)
}
```

### E-Commerce System

```kotlin
object Products : Table("products") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 200).notNull()
    val sku = varchar("sku", 50).notNull().unique()
    val price = double("price").notNull()
    val stock = int("stock").default(0)
    val isActive = bool("is_active").default(true)
}

object Orders : Table("orders") {
    val id = int("id").primaryKey().autoIncrement()
    val userId = int("user_id").notNull().references(Users.id)
    val totalAmount = double("total_amount").notNull()
    val status = varchar("status", 20).default("pending")
    val createdAt = varchar("created_at", 50).notNull()
}

object OrderItems : Table("order_items") {
    val id = int("id").primaryKey().autoIncrement()
    val orderId = int("order_id").notNull().references(Orders.id)
    val productId = int("product_id").notNull().references(Products.id)
    val quantity = int("quantity").notNull()
    val price = double("price").notNull()
}
```

### Blog System

```kotlin
object Posts : Table("posts") {
    val id = int("id").primaryKey().autoIncrement()
    val authorId = int("author_id").notNull().references(Users.id)
    val title = varchar("title", 200).notNull()
    val slug = varchar("slug", 200).notNull().unique()
    val content = text("content").notNull()
    val excerpt = varchar("excerpt", 500)
    val status = varchar("status", 20).default("draft")
    val publishedAt = varchar("published_at", 50)
    val createdAt = varchar("created_at", 50).notNull()
}

object Comments : Table("comments") {
    val id = int("id").primaryKey().autoIncrement()
    val postId = int("post_id").notNull().references(Posts.id)
    val userId = int("user_id").notNull().references(Users.id)
    val content = text("content").notNull()
    val createdAt = varchar("created_at", 50).notNull()
}
```
