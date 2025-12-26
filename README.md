# KORM DSL 🚀

**Kotlin Object-Relational Mapping** - A modern, type-safe DSL for database operations in Kotlin.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-21-orange.svg)](https://openjdk.org/)

---

## ✨ Features

- 🎯 **Type-Safe DSL** - Compile-time safety for all database operations
- 🗄️ **Multi-Database Support** - PostgreSQL, MySQL, SQLite, H2
- 🔗 **Advanced Joins** - INNER, LEFT, RIGHT, FULL OUTER joins with intuitive syntax
- 📊 **Aggregations** - COUNT, SUM, AVG, MIN, MAX with GROUP BY and HAVING
- ⚡ **Batch Operations** - High-performance bulk inserts and updates
- 🔄 **Transactions** - Automatic commit/rollback with exception handling
- ✅ **Validation Framework** - Built-in data validation with custom rules
- 🔧 **Schema Migrations** - Version-controlled database schema changes
- 📈 **Monitoring & Profiling** - Track performance, detect slow queries, collect metrics
- 🚀 **Advanced Queries** - Window functions, CTEs, CASE expressions, advanced aggregates
- 💾 **Query Caching** - LRU cache with TTL support for improved performance
- 📦 **Connection Pooling** - HikariCP integration for optimal performance
- 🎨 **Clean API** - Intuitive, fluent interface for building queries

---

## 🚀 Quick Start

### 1. Add Dependencies

**Gradle (Kotlin DSL):**
```kotlin
dependencies {
    implementation("com.korm:korm-dsl-core:0.1.0")
    
    // Database drivers
    implementation("com.h2database:h2:2.3.232")
    // OR
    implementation("org.postgresql:postgresql:42.7.4")
    // OR
    implementation("com.mysql:mysql-connector-j:9.1.0")
}
```

### 2. Define Your Schema

```kotlin
import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val age = int("age")
}
```

### 3. Connect to Database

```kotlin
import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect

val pool = ConnectionPool.create(
    url = "jdbc:h2:mem:test",
    driver = "org.h2.Driver"
)

val db = Database(H2Dialect, pool)
```

### 4. Perform CRUD Operations

```kotlin
import com.korm.dsl.schema.create
import com.korm.dsl.query.*

// Create table
Users.create(db)

// Insert
Users.insert(db)
    .set(Users.name, "Alice")
    .set(Users.email, "alice@example.com")
    .set(Users.age, 30)
    .execute()

// Select
val users = Users.select(db)
    .where(Users.age, 30)
    .execute { rs ->
        User(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            age = rs.getInt("age")
        )
    }

// Update
Users.update(db)
    .set(Users.age, 31)
    .where(Users.email, "alice@example.com")
    .execute()

// Delete
Users.delete(db)
    .where(Users.id, 1)
    .execute()
```

---

## 📖 Documentation

### Core Concepts

- **[Getting Started](docs/getting-started/quick-start.md)** - Installation and first steps
- **[Table Definition](docs/core-concepts/table-definition.md)** - Define database schemas
- **[Queries](docs/core-concepts/queries.md)** - SELECT, INSERT, UPDATE, DELETE
- **[Relationships](docs/core-concepts/relationships.md)** - JOINs and foreign keys
- **[Transactions](docs/core-concepts/transactions.md)** - Transaction management

### Advanced Features

- **[Validation](docs/advanced/validation.md)** - Data validation framework
- **[Performance](docs/advanced/performance.md)** - Batch operations and optimization

### Examples

Explore working examples in the [`examples/`](examples/) directory:

- **[example-basic](examples/example-basic/)** - Simple CRUD operations
- **[example-relationships](examples/example-relationships/)** - JOINs and foreign keys
- **[example-aggregates](examples/example-aggregates/)** - Aggregations and GROUP BY
- **[example-advanced](examples/example-advanced/)** - Validation, batch operations

---

## 💡 Usage Examples

### JOINs

```kotlin
// Books with their authors
val booksWithAuthors = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .execute { rs ->
        BookWithAuthor(
            bookTitle = rs.getString("title"),
            authorName = rs.getString("name")
        )
    }
```

### Aggregations

```kotlin
// User statistics
val stats = Orders.select(db)
    .selectWithAggregate(
        Users.username,
        aggregates = listOf(
            count(alias = "order_count"),
            sum(Orders.totalAmount, alias = "total_spent"),
            avg(Orders.totalAmount, alias = "avg_order")
        )
    )
    .innerJoinOn(Users, Orders.userId, Users.id)
    .groupBy(Users.username, Users.id)
    .having(count(), ">", 5)
    .execute { rs ->
        mapOf(
            "username" to rs.getString("username"),
            "orders" to rs.getInt("order_count"),
            "total" to rs.getDouble("total_spent")
        )
    }
```

### Batch Operations

```kotlin
// Batch insert 1000 records
val batchInsert = Users.batchInsert(db)
repeat(1000) { i ->
    batchInsert.addBatch {
        set(Users.name, "User$i")
        set(Users.email, "user$i@example.com")
        set(Users.age, 20 + (i % 50))
    }
}
batchInsert.execute(batchSize = 100)
```

### Validation

```kotlin
val emailValidator = Validator<String>()
    .addRule(notNull("email"))
    .addRule(email("email"))
    .addRule(stringLength("email", max = 255))

val result = emailValidator.validate("user@example.com")
if (result.isValid()) {
    // Proceed with valid data
}
```

### Transactions

```kotlin
db.transaction { conn ->
    // All operations in this block are transactional
    Users.insert(db).set(Users.name, "Alice").execute()
    Orders.insert(db).set(Orders.userId, 1).execute()
    // Auto-commits on success, auto-rolls back on exception
}
```

---

## 🗄️ Supported Databases

| Database   | Dialect          | Driver                          |
|------------|------------------|---------------------------------|
| PostgreSQL | `PostgresDialect`| `org.postgresql:postgresql`     |
| MySQL      | `MySQLDialect`   | `com.mysql:mysql-connector-j`   |
| SQLite     | `SQLiteDialect`  | `org.xerial:sqlite-jdbc`        |
| H2         | `H2Dialect`      | `com.h2database:h2`             |

---

## 🏗️ Architecture

```
korm-dsl/
├── korm-dsl-core/              # Core ORM functionality
│   ├── core/                   # Database & connection management
│   ├── dialect/                # Database-specific SQL generation
│   ├── schema/                 # Table/Column definitions
│   ├── query/                  # Query builders
│   ├── expressions/            # Aggregate expressions
│   └── validation/             # Validation framework
│
└── examples/                   # Example applications
    ├── example-basic/
    ├── example-relationships/
    ├── example-aggregates/
    └── example-advanced/
```

---

## 🎯 Roadmap

### Current Version (0.1.0)
- ✅ Core CRUD operations
- ✅ JOINs (all types)
- ✅ Aggregations & GROUP BY
- ✅ Batch operations
- ✅ Subqueries & UNION
- ✅ Validation framework
- ✅ Transaction management
- ✅ Schema migrations
- ✅ Monitoring & profiling
- ✅ Advanced queries (Window functions, CTEs, CASE)
- ✅ Query result caching

### Upcoming Features
- 📱 Multiplatform support (JVM, Android, Native)
- 🔐 Advanced security features
- 🔄 Replication support
- 📡 Event streaming

---

## 🤝 Contributing

Contributions are welcome! See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

---

## 📄 License

KORM DSL is licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

---

## 🙏 Acknowledgments

- Built with [Kotlin](https://kotlinlang.org/)
- Connection pooling by [HikariCP](https://github.com/brettwooldridge/HikariCP)
- Inspired by [Exposed](https://github.com/JetBrains/Exposed) and [jOOQ](https://www.jooq.org/)

---

## 📞 Support

- 💬 Discussions: [GitHub Discussions](https://github.com/yourusername/korm-dsl/discussions)
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/korm-dsl/issues)

---

**Made with ❤️ by the KORM team**
