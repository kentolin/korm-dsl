# Queries

Comprehensive guide to querying your database with KORM DSL.

---

## SELECT Queries

### Basic SELECT

```kotlin
// Select all users
val users = Users.select(db).execute { rs ->
    User(
        id = rs.getInt("id"),
        name = rs.getString("name"),
        email = rs.getString("email")
    )
}
```

### SELECT with WHERE

```kotlin
// Single condition
val user = Users.select(db)
    .where(Users.email, "alice@example.com")
    .execute { rs ->
        User(/* ... */)
    }

// Multiple conditions (AND)
val adults = Users.select(db)
    .where(Users.age, 18)
    .whereRaw("country = ?", "USA")
    .execute { rs -> /* ... */ }
```

### SELECT Specific Columns

```kotlin
// Select only name and email
val result = Users.select(db)
    .select(Users.name, Users.email)
    .execute { rs ->
        rs.getString("name") to rs.getString("email")
    }
```

### LIMIT and OFFSET

```kotlin
// Get first 10 users
val first10 = Users.select(db)
    .limit(10)
    .execute { rs -> /* ... */ }

// Pagination
val page2 = Users.select(db)
    .limit(10)
    .offset(10)
    .execute { rs -> /* ... */ }
```

### ORDER BY

```kotlin
// Ascending order
val sortedAsc = Users.select(db)
    .orderBy(Users.name, asc = true)
    .execute { rs -> /* ... */ }

// Descending order
val sortedDesc = Users.select(db)
    .orderBy(Users.createdAt, asc = false)
    .execute { rs -> /* ... */ }
```

---

## INSERT Queries

### Basic INSERT

```kotlin
Users.insert(db)
    .set(Users.name, "Alice")
    .set(Users.email, "alice@example.com")
    .set(Users.age, 30)
    .execute()
```

### Batch INSERT

```kotlin
val batchInsert = Users.batchInsert(db)
repeat(1000) { i ->
    batchInsert.addBatch {
        set(Users.name, "User$i")
        set(Users.email, "user$i@example.com")
        set(Users.age, 20 + (i % 50))
    }
}
// Execute in batches of 100
val inserted = batchInsert.execute(batchSize = 100)
println("Inserted $inserted records")
```

---

## UPDATE Queries

### Basic UPDATE

```kotlin
Users.update(db)
    .set(Users.age, 31)
    .where(Users.email, "alice@example.com")
    .execute()
```

### UPDATE Multiple Columns

```kotlin
Users.update(db)
    .set(Users.name, "Alice Smith")
    .set(Users.age, 31)
    .set(Users.email, "alice.smith@example.com")
    .where(Users.id, 1)
    .execute()
```

### Batch UPDATE

```kotlin
val batchUpdate = Users.batchUpdate(db)
for (i in 1..100) {
    batchUpdate.addBatch(
        updates = mapOf(Users.age to 30),
        whereColumn = Users.id,
        whereValue = i
    )
}
val updated = batchUpdate.execute()
println("Updated $updated records")
```

---

## DELETE Queries

### Basic DELETE

```kotlin
Users.delete(db)
    .where(Users.email, "alice@example.com")
    .execute()
```

### DELETE with Multiple Conditions

```kotlin
Users.delete(db)
    .where(Users.age, 18)
    .whereRaw("created_at < ?", "2020-01-01")
    .execute()
```

---

## Aggregates

### COUNT

```kotlin
// Count all
val total = Users.select(db)
    .selectAggregate(count(alias = "total"))
    .execute { rs -> rs.getInt("total") }
    .first()

// Count distinct
val uniqueCountries = Users.select(db)
    .selectAggregate(count(Users.country, distinct = true, alias = "countries"))
    .execute { rs -> rs.getInt("countries") }
    .first()
```

### SUM

```kotlin
val totalRevenue = Orders.select(db)
    .selectAggregate(sum(Orders.amount, alias = "total"))
    .execute { rs -> rs.getDouble("total") }
    .first()
```

### AVG

```kotlin
val avgAge = Users.select(db)
    .selectAggregate(avg(Users.age, alias = "average"))
    .execute { rs -> rs.getDouble("average") }
    .first()
```

### MIN and MAX

```kotlin
val priceRange = Products.select(db)
    .selectAggregate(
        min(Products.price, alias = "min_price"),
        max(Products.price, alias = "max_price")
    )
    .execute { rs ->
        rs.getDouble("min_price") to rs.getDouble("max_price")
    }
    .first()
```

---

## GROUP BY

### Basic GROUP BY

```kotlin
val ordersByStatus = Orders.select(db)
    .selectAggregate(count(alias = "count"))
    .groupBy(Orders.status)
    .execute { rs ->
        rs.getString("status") to rs.getInt("count")
    }
```

### Multiple Aggregates

```kotlin
val userStats = Orders.select(db)
    .selectAggregate(
        count(alias = "order_count"),
        sum(Orders.amount, alias = "total_spent"),
        avg(Orders.amount, alias = "avg_order")
    )
    .groupBy(Orders.userId)
    .execute { rs ->
        mapOf(
            "userId" to rs.getInt("user_id"),
            "orders" to rs.getInt("order_count"),
            "total" to rs.getDouble("total_spent"),
            "average" to rs.getDouble("avg_order")
        )
    }
```

---

## HAVING Clause

```kotlin
// Find users with more than 10 orders
val powerUsers = Orders.select(db)
    .selectAggregate(count(alias = "order_count"))
    .groupBy(Orders.userId)
    .having(count(), ">", 10)
    .execute { rs ->
        rs.getInt("user_id") to rs.getInt("order_count")
    }
```

---

## Subqueries

### WHERE IN Subquery

```kotlin
// Find users who have placed orders
val subquery = Subquery.from(
    Orders.select(db)
        .select(Orders.userId)
)

val activeUsers = Users.select(db)
    .whereIn(Users.id, subquery)
    .execute { rs -> /* ... */ }
```

### WHERE NOT IN

```kotlin
// Find users who have NOT placed orders
val inactiveUsers = Users.select(db)
    .whereNotIn(Users.id, subquery)
    .execute { rs -> /* ... */ }
```

### WHERE EXISTS

```kotlin
val subquery = Subquery.from(
    Orders.select(db)
        .where(Orders.userId, Users.id)
)

val usersWithOrders = Users.select(db)
    .whereExists(subquery)
    .execute { rs -> /* ... */ }
```

---

## UNION Queries

### UNION (Distinct)

```kotlin
val query1 = Users.select(db).where(Users.age, 30)
val query2 = Users.select(db).where(Users.country, "USA")

val combined = union(db, query1, query2)
    .execute { rs -> /* ... */ }
```

### UNION ALL (Include Duplicates)

```kotlin
val allResults = unionAll(db, query1, query2)
    .execute { rs -> /* ... */ }
```

---

## Raw Queries

### Execute Raw Result

```kotlin
// Get results as Map<String, Any?>
val results = Users.select(db)
    .whereRaw("age > ? AND country = ?", 25, "USA")
    .executeRaw()

results.forEach { row ->
    println("Name: ${row["name"]}, Age: ${row["age"]}")
}
```

---

## Best Practices

### 1. Use Type-Safe Mappers

```kotlin
// ✅ GOOD - Type-safe mapping
data class User(val id: Int, val name: String, val email: String)

val users = Users.select(db).execute { rs ->
    User(
        id = rs.getInt("id"),
        name = rs.getString("name"),
        email = rs.getString("email")
    )
}

// ❌ BAD - Untyped maps
val users = Users.select(db).executeRaw()
```

### 2. Always Use WHERE for DELETE/UPDATE

```kotlin
// ✅ GOOD - Specific WHERE clause
Users.update(db)
    .set(Users.age, 30)
    .where(Users.id, 1)
    .execute()

// ⚠️ DANGEROUS - Updates ALL rows!
Users.update(db)
    .set(Users.age, 30)
    .execute()
```

### 3. Use Batch Operations for Bulk Insert

```kotlin
// ✅ GOOD - Batch insert
val batch = Users.batchInsert(db)
users.forEach { user ->
    batch.addBatch {
        set(Users.name, user.name)
        set(Users.email, user.email)
    }
}
batch.execute(batchSize = 100)

// ❌ BAD - Individual inserts (slow!)
users.forEach { user ->
    Users.insert(db)
        .set(Users.name, user.name)
        .set(Users.email, user.email)
        .execute()
}
```

---

## Performance Tips

### Pagination

```kotlin
fun getUsers(page: Int, pageSize: Int = 20): List<User> {
    return Users.select(db)
        .limit(pageSize)
        .offset(page * pageSize)
        .orderBy(Users.id, asc = true)
        .execute { rs -> /* ... */ }
}
```

### Counting with Pagination

```kotlin
// Get total count
val total = Users.select(db)
    .selectAggregate(count(alias = "total"))
    .execute { rs -> rs.getInt("total") }
    .first()

// Get page
val users = getUsers(page = 0, pageSize = 20)

println("Showing ${users.size} of $total users")
```

---

## Next Steps

- **[Relationships](relationships.md)** - Learn about JOINs
- **[Transactions](transactions.md)** - Manage transactions
- **[Validation](../advanced/validation.md)** - Validate data before inserting
