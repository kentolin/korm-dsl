# korm-dsl/examples/example-transactions/README.md

# KORM Transactions Example

Comprehensive examples demonstrating transaction management with KORM DSL.

## Features

- Basic ACID transactions
- Automatic rollback on errors
- Transaction isolation levels
- Nested transactions
- Money transfers with proper locking
- Deadlock prevention
- Savepoints for partial rollback
- Batch operations

## Running

### Start Database
```bash
docker-compose up -d
```

### Run Examples
```bash
./gradlew :examples:example-transactions:run
```

## Examples Covered

### 1. Basic Transaction
- ACID properties
- Atomic commits
- Simple CRUD operations

### 2. Rollback
- Automatic rollback on exceptions
- Explicit rollback
- Error handling

### 3. Isolation Levels
- READ_COMMITTED
- REPEATABLE_READ
- SERIALIZABLE
- Non-repeatable reads
- Phantom reads

### 4. Nested Transactions
- Multiple operations
- Partial success/failure
- Complete rollback

### 5. Money Transfers
- Proper account locking
- Insufficient funds handling
- Transfer logging
- Optimistic locking

### 6. Deadlock Prevention
- Lock ordering
- Concurrent transfers
- Deadlock avoidance strategies

### 7. Savepoints
- Partial rollback
- Nested savepoints
- Complex transaction flows

### 8. Batch Operations
- Bulk inserts
- Bulk updates
- Performance optimization

## Key Concepts

### ACID Properties
- **Atomicity**: All or nothing
- **Consistency**: Valid state transitions
- **Isolation**: Concurrent execution
- **Durability**: Persistent changes

### Lock Ordering
Prevents deadlocks by acquiring locks in a consistent order:
```kotlin
val (firstId, secondId) = if (fromAccountId < toAccountId) {
    fromAccountId to toAccountId
} else {
    toAccountId to fromAccountId
}
```

### Optimistic Locking
Uses version numbers to detect concurrent modifications:
```kotlin
val account = findById(id)
update(Accounts) {
    it[Accounts.balance] = newBalance
    it[Accounts.version] = account.version + 1
}.where(
    (Accounts.id eq id) and 
    (Accounts.version eq account.version)
)
```

## See Also

- [Transaction Guide](../../docs/advanced/transactions.md)
- [ACID Properties](../../docs/concepts/acid.md)
- [Isolation Levels](../../docs/concepts/isolation-levels.md)
