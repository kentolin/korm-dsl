# Performance Optimization

Best practices and techniques for optimizing KORM DSL application performance.

---

## Connection Pooling

### Configure Pool Size

```kotlin
val pool = ConnectionPool.create(
    url = "jdbc:postgresql://localhost:5432/mydb",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "password",
    maximumPoolSize = 20  // Tune based on workload
)
```

### Pool Size Guidelines

- **Low traffic**: 5-10 connections
- **Medium traffic**: 10-20 connections
- **High traffic**: 20-50 connections

**Formula**: `connections = (core_count * 2) + effective_spindle_count`

---

## Batch Operations

### Use Batch INSERT

```kotlin
// ❌ BAD - Individual inserts (slow!)
users.forEach { user ->
    Users.insert(db)
        .set(Users.name, user.name)
        .execute()
}
// 1000 users = 1000 round trips to database

// ✅ GOOD - Batch insert (fast!)
val batch = Users.batchInsert(db)
users.forEach { user ->
    batch.addBatch {
        set(Users.name, user.name)
    }
}
batch.execute(batchSize = 100)
// 1000 users = 10 batches (10 round trips)
```

### Tune Batch Size

```kotlin
// Small batches for limited memory
batch.execute(batchSize = 50)

// Larger batches for better performance
batch.execute(batchSize = 500)

// Very large batches (be careful with memory)
batch.execute(batchSize = 1000)
```

**Recommended batch size**: 100-500 rows

---

## Query Optimization

### Use SELECT with Specific Columns

```kotlin
// ❌ BAD - Fetches all columns
val users = Users.select(db)
    .execute { rs ->
        User(
            id = rs.getInt("id"),
            name = rs.getString("name")
        )
    }

// ✅ GOOD - Fetches only needed columns
val users = Users.select(db)
    .select(Users.id, Users.name)
    .execute { rs ->
        User(
            id = rs.getInt("id"),
            name = rs.getString("name")
        )
    }
```

### Use LIMIT for Large Result Sets

```kotlin
// ❌ BAD - Loads all million rows into memory
val allUsers = Users.select(db).execute { /* ... */ }

// ✅ GOOD - Pagination
fun getUsersPage(page: Int, pageSize: Int = 100): List<User> {
    return Users.select(db)
        .limit(pageSize)
        .offset(page * pageSize)
        .execute { /* ... */ }
}
```

---

## JOIN Optimization

### Avoid N+1 Queries

```kotlin
// ❌ BAD - N+1 query problem
val books = Books.select(db).execute { /* ... */ }
books.forEach { book ->
    // Separate query for EACH book!
    val author = Authors.select(db)
        .where(Authors.id, book.authorId)
        .execute { /* ... */ }
}
// 100 books = 101 queries (1 + 100)

// ✅ GOOD - Single JOIN query
val booksWithAuthors = Books.select(db)
    .innerJoinOn(Authors, Books.authorId, Authors.id)
    .execute { /* ... */ }
// 100 books = 1 query
```

### Index Foreign Keys

Ensure foreign key columns are indexed:

```sql
CREATE INDEX idx_books_author_id ON books(author_id);
CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_comments_post_id ON comments(post_id);
```

---

## Indexing

### Create Indexes for Frequent Queries

```sql
-- Index on frequently queried columns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Composite index for multiple column queries
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- Index on foreign keys
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
```

### When to Index

✅ **Index when:**
- Column used in WHERE clauses frequently
- Column used in JOIN conditions
- Column used in ORDER BY frequently
- Foreign key columns

❌ **Don't index when:**
- Table is very small (<1000 rows)
- Column has very low cardinality (e.g., boolean)
- Table has frequent INSERTs/UPDATEs (indexes slow these down)

---

## Transaction Optimization

### Keep Transactions Short

```kotlin
// ❌ BAD - Long transaction
db.transaction { conn ->
    Users.insert(db).set(Users.name, "Alice").execute()
    
    // Expensive computation in transaction!
    Thread.sleep(5000)
    val result = complexCalculation()
    
    UserStats.insert(db).set(UserStats.data, result).execute()
}

// ✅ GOOD - Short transaction
val result = complexCalculation()  // Outside transaction
db.transaction { conn ->
    Users.insert(db).set(Users.name, "Alice").execute()
    UserStats.insert(db).set(UserStats.data, result).execute()
}
```

### Batch Operations in Transactions

```kotlin
// Combine batch operations with transactions for consistency
db.transaction { conn ->
    val userBatch = Users.batchInsert(db)
    users.forEach { user ->
        userBatch.addBatch {
            set(Users.name, user.name)
        }
    }
    userBatch.execute(batchSize = 100)
}
```

---

## Caching (Future Feature)

```kotlin
// Coming soon: Query result caching
val users = Users.select(db)
    .where(Users.active, true)
    .cache(ttl = 5.minutes)
    .execute { /* ... */ }
```

---

## Database-Specific Optimizations

### PostgreSQL

```kotlin
// Use EXPLAIN ANALYZE to understand query performance
val sql = """
    EXPLAIN ANALYZE
    SELECT * FROM users WHERE email = 'alice@example.com'
"""

// Create partial indexes
CREATE INDEX idx_active_users ON users(email) WHERE active = true;

// Use connection pooling with prepared statements
val pool = ConnectionPool.create(
    url = "jdbc:postgresql://localhost:5432/mydb?prepareThreshold=3",
    driver = "org.postgresql.Driver",
    maximumPoolSize = 20
)
```

### MySQL

```kotlin
// Enable query cache
val pool = ConnectionPool.create(
    url = "jdbc:mysql://localhost:3306/mydb?useServerPrepStmts=true&cachePrepStmts=true",
    driver = "com.mysql.cj.jdbc.Driver",
    maximumPoolSize = 20
)
```

---

## Monitoring Performance

### Measure Query Execution Time

```kotlin
fun <T> measureQuery(name: String, block: () -> T): T {
    val start = System.currentTimeMillis()
    val result = block()
    val duration = System.currentTimeMillis() - start
    println("Query '$name' took ${duration}ms")
    return result
}

// Usage
val users = measureQuery("getAllUsers") {
    Users.select(db).execute { /* ... */ }
}
```

### Log Slow Queries

```kotlin
class QueryLogger {
    fun <T> logIfSlow(threshold: Long = 1000, name: String, block: () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val duration = System.currentTimeMillis() - start
        
        if (duration > threshold) {
            logger.warn("Slow query '$name' took ${duration}ms (threshold: ${threshold}ms)")
        }
        
        return result
    }
}
```

---

## Memory Optimization

### Stream Large Result Sets

```kotlin
// For very large result sets, process row by row
fun processLargeDataset() {
    Users.select(db).execute { rs ->
        while (rs.next()) {
            val user = User(
                id = rs.getInt("id"),
                name = rs.getString("name")
            )
            // Process user immediately
            processUser(user)
            // Don't accumulate all in memory!
        }
    }
}
```

### Use Pagination

```kotlin
fun getAllUsersInPages(pageSize: Int = 1000) {
    var page = 0
    var hasMore = true
    
    while (hasMore) {
        val users = Users.select(db)
            .limit(pageSize)
            .offset(page * pageSize)
            .orderBy(Users.id, asc = true)
            .execute { /* ... */ }
        
        if (users.isEmpty()) {
            hasMore = false
        } else {
            processUsers(users)
            page++
        }
    }
}
```

---

## Best Practices

### 1. Use Connection Pooling Always

```kotlin
// ✅ GOOD - Reuse connections
val pool = ConnectionPool.create(...)
val db = Database(dialect, pool)

// ❌ BAD - Create new connection each time
fun getUsers() {
    val pool = ConnectionPool.create(...)  // DON'T DO THIS!
    val db = Database(dialect, pool)
    return Users.select(db).execute { /* ... */ }
}
```

### 2. Close Resources

```kotlin
// ✅ GOOD - Use try-finally or .use
val pool = ConnectionPool.create(...)
val db = Database(dialect, pool)
try {
    // Use database
} finally {
    db.close()
}

// Or with .use
ConnectionPool.create(...).use { pool ->
    val db = Database(dialect, pool)
    // Use database
}
```

### 3. Batch Similar Operations

```kotlin
// ✅ GOOD - Batch similar operations
val batch = Users.batchInsert(db)
newUsers.forEach { batch.addBatch { /* ... */ } }
batch.execute()

// ❌ BAD - Mix operations
newUsers.forEach { user ->
    Users.insert(db).set(Users.name, user.name).execute()
    UserStats.insert(db).set(UserStats.userId, user.id).execute()
}
```

### 4. Use Appropriate Data Types

```kotlin
// ✅ GOOD - Use appropriate types
object Products : Table("products") {
    val price = double("price")      // For money
    val quantity = int("quantity")   // For counts
}

// ❌ BAD - Using strings for everything
object Products : Table("products") {
    val price = varchar("price", 50)     // Inefficient!
    val quantity = varchar("quantity", 50)
}
```

---

## Performance Checklist

Before deploying to production:

- [ ] Connection pool configured appropriately
- [ ] Indexes created on frequently queried columns
- [ ] Foreign keys indexed
- [ ] Batch operations used for bulk inserts/updates
- [ ] Pagination implemented for large result sets
- [ ] Transactions kept short
- [ ] Query performance measured and optimized
- [ ] No N+1 query problems
- [ ] Specific columns selected (not SELECT *)
- [ ] Resources properly closed

---

## Benchmarking

### Simple Benchmark

```kotlin
fun benchmarkInserts(count: Int) {
    // Individual inserts
    val start1 = System.currentTimeMillis()
    repeat(count) { i ->
        Users.insert(db)
            .set(Users.name, "User$i")
            .execute()
    }
    val duration1 = System.currentTimeMillis() - start1
    
    // Batch inserts
    val start2 = System.currentTimeMillis()
    val batch = Users.batchInsert(db)
    repeat(count) { i ->
        batch.addBatch {
            set(Users.name, "User$i")
        }
    }
    batch.execute(batchSize = 100)
    val duration2 = System.currentTimeMillis() - start2
    
    println("Individual: ${duration1}ms")
    println("Batch: ${duration2}ms")
    println("Speedup: ${duration1.toDouble() / duration2}x")
}

// Example output:
// Individual: 5234ms
// Batch: 892ms
// Speedup: 5.9x
```

---

## Next Steps

- **[Caching](caching.md)** (Coming Soon) - Implement query caching
- **[Monitoring](monitoring.md)** (Coming Soon) - Monitor database performance
