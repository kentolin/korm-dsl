# Transactions

Learn how to manage database transactions for data consistency and integrity.

---

## Basic Transaction

KORM provides automatic transaction management with auto-commit and auto-rollback.

```kotlin
import com.korm.dsl.core.transaction

db.transaction { conn ->
    // All operations in this block are part of the same transaction
    Users.insert(db)
        .set(Users.name, "Alice")
        .set(Users.email, "alice@example.com")
        .execute()
    
    Orders.insert(db)
        .set(Orders.userId, 1)
        .set(Orders.amount, 99.99)
        .execute()
    
    // If we reach here without exceptions, transaction commits automatically
    // If any exception occurs, transaction rolls back automatically
}
```

---

## How Transactions Work

### Automatic Commit

If the transaction block completes without exceptions:

```kotlin
db.transaction { conn ->
    Users.insert(db).set(Users.name, "Bob").execute()
    // ✓ Transaction commits automatically here
}
println("User Bob saved successfully")
```

### Automatic Rollback

If an exception occurs, changes are rolled back:

```kotlin
try {
    db.transaction { conn ->
        Users.insert(db).set(Users.name, "Charlie").execute()
        
        // This will throw an exception (e.g., duplicate email)
        Users.insert(db)
            .set(Users.name, "Dave")
            .set(Users.email, "charlie@example.com")
            .execute()
        
        // ✗ We never reach here
    }
} catch (e: Exception) {
    println("Transaction rolled back: ${e.message}")
    // Charlie was NOT inserted - entire transaction rolled back
}
```

---

## Real-World Examples

### Banking Transfer

```kotlin
fun transferMoney(fromAccountId: Int, toAccountId: Int, amount: Double) {
    db.transaction { conn ->
        // Debit from account
        Accounts.update(db)
            .set(Accounts.balance, Accounts.balance - amount)
            .where(Accounts.id, fromAccountId)
            .execute()
        
        // Check if account has sufficient funds
        val fromAccount = Accounts.select(db)
            .where(Accounts.id, fromAccountId)
            .execute { rs -> rs.getDouble("balance") }
            .first()
        
        if (fromAccount < 0) {
            throw InsufficientFundsException("Not enough balance")
        }
        
        // Credit to account
        Accounts.update(db)
            .set(Accounts.balance, Accounts.balance + amount)
            .where(Accounts.id, toAccountId)
            .execute()
        
        // Log the transaction
        TransactionLog.insert(db)
            .set(TransactionLog.fromAccountId, fromAccountId)
            .set(TransactionLog.toAccountId, toAccountId)
            .set(TransactionLog.amount, amount)
            .execute()
        
        // Commits if we reach here
    }
}
```

### E-Commerce Order

```kotlin
fun createOrder(userId: Int, items: List<OrderItem>): Int {
    return db.transaction { conn ->
        // Create order
        val orderId = Orders.insert(db)
            .set(Orders.userId, userId)
            .set(Orders.totalAmount, items.sumOf { it.price * it.quantity })
            .set(Orders.status, "pending")
            .execute()
        
        // Insert order items
        items.forEach { item ->
            OrderItems.insert(db)
                .set(OrderItems.orderId, orderId)
                .set(OrderItems.productId, item.productId)
                .set(OrderItems.quantity, item.quantity)
                .set(OrderItems.price, item.price)
                .execute()
            
            // Update product stock
            Products.update(db)
                .set(Products.stock, Products.stock - item.quantity)
                .where(Products.id, item.productId)
                .execute()
        }
        
        // Return order ID
        orderId
    }
}
```

### User Registration

```kotlin
fun registerUser(username: String, email: String, password: String): User {
    return db.transaction { conn ->
        // Create user account
        val userId = Users.insert(db)
            .set(Users.username, username)
            .set(Users.email, email)
            .set(Users.passwordHash, hashPassword(password))
            .execute()
        
        // Create user profile
        UserProfiles.insert(db)
            .set(UserProfiles.userId, userId)
            .set(UserProfiles.displayName, username)
            .set(UserProfiles.bio, "")
            .execute()
        
        // Assign default role
        UserRoles.insert(db)
            .set(UserRoles.userId, userId)
            .set(UserRoles.roleId, DEFAULT_ROLE_ID)
            .execute()
        
        // Send welcome email (non-transactional)
        sendWelcomeEmail(email)
        
        // Return complete user
        User(userId, username, email)
    }
}
```

---

## Nested Operations

Transactions can contain complex operations:

```kotlin
db.transaction { conn ->
    // Multiple inserts
    val userId = Users.insert(db)
        .set(Users.name, "Alice")
        .execute()
    
    // Batch operations within transaction
    val batchInsert = Posts.batchInsert(db)
    repeat(10) { i ->
        batchInsert.addBatch {
            set(Posts.userId, userId)
            set(Posts.title, "Post $i")
            set(Posts.content, "Content $i")
        }
    }
    batchInsert.execute()
    
    // Updates
    Users.update(db)
        .set(Users.postCount, 10)
        .where(Users.id, userId)
        .execute()
    
    // Queries
    val posts = Posts.select(db)
        .where(Posts.userId, userId)
        .execute { rs -> rs.getInt("id") }
    
    // All or nothing!
}
```

---

## Error Handling

### Catch Specific Exceptions

```kotlin
try {
    db.transaction { conn ->
        Users.insert(db)
            .set(Users.email, "duplicate@example.com")
            .execute()
    }
} catch (e: SQLException) {
    when {
        e.message?.contains("duplicate", ignoreCase = true) == true -> {
            println("Email already exists")
        }
        else -> {
            println("Database error: ${e.message}")
        }
    }
}
```

### Custom Business Logic Exceptions

```kotlin
class InsufficientFundsException(message: String) : Exception(message)
class AccountNotFoundException(message: String) : Exception(message)

fun processPayment(accountId: Int, amount: Double) {
    try {
        db.transaction { conn ->
            val account = Accounts.select(db)
                .where(Accounts.id, accountId)
                .execute { rs -> 
                    Account(rs.getInt("id"), rs.getDouble("balance"))
                }
                .firstOrNull() ?: throw AccountNotFoundException("Account $accountId not found")
            
            if (account.balance < amount) {
                throw InsufficientFundsException("Balance: ${account.balance}, Required: $amount")
            }
            
            Accounts.update(db)
                .set(Accounts.balance, account.balance - amount)
                .where(Accounts.id, accountId)
                .execute()
        }
        println("Payment processed successfully")
    } catch (e: InsufficientFundsException) {
        println("Payment failed: ${e.message}")
    } catch (e: AccountNotFoundException) {
        println("Error: ${e.message}")
    }
}
```

---

## Best Practices

### 1. Keep Transactions Short

```kotlin
// ✅ GOOD - Short, focused transaction
db.transaction { conn ->
    Users.insert(db).set(Users.name, "Alice").execute()
    UserProfiles.insert(db).set(UserProfiles.userId, 1).execute()
}

// ❌ BAD - Long-running transaction
db.transaction { conn ->
    Users.insert(db).set(Users.name, "Alice").execute()
    
    // Don't do heavy computation in transaction!
    Thread.sleep(5000)
    val hash = computeExpensiveHash(data)
    
    UserProfiles.insert(db).set(UserProfiles.hash, hash).execute()
}
```

### 2. Avoid External API Calls

```kotlin
// ✅ GOOD - External calls outside transaction
val userId = db.transaction { conn ->
    Users.insert(db).set(Users.name, "Alice").execute()
}
// Email sent after transaction commits
sendWelcomeEmail(userId)

// ❌ BAD - External API call inside transaction
db.transaction { conn ->
    val userId = Users.insert(db).set(Users.name, "Alice").execute()
    sendWelcomeEmail(userId)  // Holds transaction open during network call!
}
```

### 3. Use Explicit Error Handling

```kotlin
// ✅ GOOD - Clear error handling
try {
    db.transaction { conn ->
        // Transaction code
    }
    println("Success")
} catch (e: SQLException) {
    logger.error("Transaction failed", e)
    // Handle error appropriately
}

// ❌ BAD - Silent failure
db.transaction { conn ->
    try {
        // Swallowing exceptions prevents rollback!
    } catch (e: Exception) {
        // Silent failure
    }
}
```

### 4. Return Values from Transactions

```kotlin
// ✅ GOOD - Return meaningful values
val orderId = db.transaction { conn ->
    val id = Orders.insert(db)
        .set(Orders.userId, 1)
        .execute()
    id
}
println("Created order: $orderId")
```

---

## Transaction Isolation (Future Feature)

```kotlin
// Coming soon: Custom isolation levels
db.transaction(isolation = IsolationLevel.REPEATABLE_READ) { conn ->
    // Transaction code
}

// Future isolation levels:
// - READ_UNCOMMITTED
// - READ_COMMITTED
// - REPEATABLE_READ
// - SERIALIZABLE
```

---

## Savepoints (Future Feature)

```kotlin
// Coming soon: Partial rollback with savepoints
db.transaction { conn ->
    Users.insert(db).set(Users.name, "Alice").execute()
    
    val savepoint = conn.setSavepoint("before_orders")
    
    try {
        Orders.insert(db).set(Orders.userId, 1).execute()
    } catch (e: Exception) {
        conn.rollback(savepoint)  // Only rollback orders, keep user
    }
}
```

---

## Common Patterns

### Batch Insert with Validation

```kotlin
fun importUsers(users: List<UserData>): ImportResult {
    var successCount = 0
    var failCount = 0
    val errors = mutableListOf<String>()
    
    users.forEach { user ->
        try {
            db.transaction { conn ->
                // Validate
                if (!isValidEmail(user.email)) {
                    throw ValidationException("Invalid email: ${user.email}")
                }
                
                // Insert
                Users.insert(db)
                    .set(Users.name, user.name)
                    .set(Users.email, user.email)
                    .execute()
                
                successCount++
            }
        } catch (e: Exception) {
            failCount++
            errors.add("${user.email}: ${e.message}")
        }
    }
    
    return ImportResult(successCount, failCount, errors)
}
```

### Conditional Operations

```kotlin
db.transaction { conn ->
    val user = Users.select(db)
        .where(Users.email, email)
        .execute { rs -> User(rs.getInt("id"), rs.getString("email")) }
        .firstOrNull()
    
    if (user == null) {
        // Create new user
        Users.insert(db)
            .set(Users.email, email)
            .set(Users.name, name)
            .execute()
    } else {
        // Update existing user
        Users.update(db)
            .set(Users.name, name)
            .where(Users.id, user.id)
            .execute()
    }
}
```

---

## Testing Transactions

```kotlin
@Test
fun `transaction should rollback on error`() {
    val initialCount = Users.select(db)
        .selectAggregate(count(alias = "total"))
        .execute { rs -> rs.getInt("total") }
        .first()
    
    try {
        db.transaction { conn ->
            Users.insert(db).set(Users.name, "Test").execute()
            throw RuntimeException("Forced error")
        }
    } catch (e: Exception) {
        // Expected
    }
    
    val finalCount = Users.select(db)
        .selectAggregate(count(alias = "total"))
        .execute { rs -> rs.getInt("total") }
        .first()
    
    assertEquals(initialCount, finalCount)  // No change!
}
```

---

## Next Steps

- **[Validation](../advanced/validation.md)** - Validate data before transactions
- **[Performance](../advanced/performance.md)** - Optimize transaction performance
