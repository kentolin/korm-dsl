# Query Result Caching

Complete guide to caching query results in KORM for improved performance.

---

## Overview

KORM provides a powerful caching system that can dramatically improve application performance by reducing database round-trips:

- 🚀 **Performance** - Cache frequently accessed query results
- 🔌 **Pluggable** - Multiple cache provider support
- ⏱️ **TTL Support** - Automatic expiration of cached entries
- 🎯 **Smart Invalidation** - Pattern-based cache invalidation
- 📊 **Statistics** - Built-in cache performance metrics
- 🔒 **Thread-Safe** - Concurrent access support

---

## Quick Start

### Basic Caching

```kotlin
import com.korm.dsl.cache.*

// Create cache manager
val cacheManager = CacheManager()

// Execute query with caching
val products = cacheManager.executeWithCache(
    db,
    "SELECT * FROM products WHERE category = ?",
    listOf("Electronics")
) { rs ->
    Product(
        id = rs.getInt("id"),
        name = rs.getString("name"),
        price = rs.getInt("price")
    )
}

// Second call returns cached results (much faster!)
val cachedProducts = cacheManager.executeWithCache(
    db,
    "SELECT * FROM products WHERE category = ?",
    listOf("Electronics")
) { rs ->
    Product(rs.getInt("id"), rs.getString("name"), rs.getInt("price"))
}
```

---

## Cache Providers

### In-Memory Cache (Default)

Built-in LRU cache with TTL support:

```kotlin
val cache = InMemoryCache(
    CacheConfig(
        maxSize = 1000,              // Maximum 1000 entries
        defaultTtl = Duration.ofMinutes(30),  // 30 minute default TTL
        evictionPolicy = EvictionPolicy.LRU   // Least Recently Used
    )
)

val cacheManager = CacheManager(cache)
```

### Cache Configuration

```kotlin
val config = CacheConfig(
    maxSize = 1000,                          // Max entries
    defaultTtl = Duration.ofMinutes(30),     // Default TTL
    enableStats = true,                      // Track statistics
    evictionPolicy = EvictionPolicy.LRU      // Eviction strategy
)
```

**Eviction Policies:**
- `LRU` - Least Recently Used (recommended)
- `LFU` - Least Frequently Used
- `FIFO` - First In First Out
- `TTL` - Time To Live based

---

## Cache Manager

### Configuration

```kotlin
val config = cacheManagerConfig {
    enableQueryCache = true
    defaultQueryTtl = Duration.ofMinutes(30)
    invalidationStrategy = TableInvalidationStrategy.Pattern
    cacheWrites = false  // Don't cache write operations
}

val cacheManager = CacheManager(
    cache = InMemoryCache(),
    config = config
)
```

### Global Cache Manager

```kotlin
// Configure global cache manager
CacheManager.configure(
    cache = InMemoryCache(CacheConfig(maxSize = 5000)),
    config = cacheManagerConfig {
        enableQueryCache = true
        defaultQueryTtl = Duration.ofHours(1)
    }
)

// Use global instance
val result = CacheManager.global().executeWithCache(...)
```

---

## Custom TTL

### Per-Query TTL

```kotlin
// Cache for 5 minutes
cacheManager.executeWithCache(
    db, sql, params,
    ttl = Duration.ofMinutes(5)
) { rs -> ... }

// Cache for 1 hour
cacheManager.executeWithCache(
    db, sql, params,
    ttl = Duration.ofHours(1)
) { rs -> ... }

// Cache for 1 day
cacheManager.executeWithCache(
    db, sql, params,
    ttl = Duration.ofDays(1)
) { rs -> ... }
```

### No Expiration

```kotlin
// Cache indefinitely (until manually invalidated)
cacheManager.executeWithCache(
    db, sql, params,
    ttl = null
) { rs -> ... }
```

---

## Cache Invalidation

### Invalidate by Table

```kotlin
// Invalidate all queries for a table
cacheManager.invalidateTable("products")

// Using extension
db.invalidateCache("products")

// From table object
Products.invalidateCache(db)
```

### Invalidate by Pattern

```kotlin
// Invalidate all product queries
cacheManager.invalidatePattern("query:*products*")

// Invalidate all user queries
cacheManager.invalidatePattern("query:*users*")
```

### Invalidate Specific Key

```kotlin
val key = CacheKeyBuilder.buildKey(sql, params)
cacheManager.invalidate(key)
```

### Clear All Cache

```kotlin
cacheManager.clear()

// Or using extension
db.clearCache()
```

---

## Cache Statistics

### Get Statistics

```kotlin
val stats = cacheManager.getStats()

println("Hits: ${stats.hits}")
println("Misses: ${stats.misses}")
println("Hit rate: ${stats.hitRate * 100}%")
println("Size: ${stats.size}/${stats.maxSize}")
```

### Print Formatted Report

```kotlin
cacheManager.printStats()
```

**Output:**

```
╔════════════════════════════════════════════════════════════════╗
║                    Cache Statistics                           ║
╚════════════════════════════════════════════════════════════════╝

Hits:          150
Misses:        50
Hit Rate:      75.00%
Miss Rate:     25.00%

Puts:          200
Evictions:     10

Size:          190/1000
Fill Rate:     19.00%

════════════════════════════════════════════════════════════════
```

---

## Cache Keys

### Automatic Key Generation

Cache keys are automatically generated from SQL and parameters:

```kotlin
// Automatically generates key like: "query:-1234567890:987654321"
cacheManager.executeWithCache(db, sql, params) { rs -> ... }
```

### Custom Keys

```kotlin
// Build your own keys
val key = CacheKeyBuilder.buildKey(sql, params)
val tableKey = CacheKeyBuilder.buildTableKey("products")
val pattern = CacheKeyBuilder.buildTablePattern("products")
```

---

## Integration Examples

### Repository Pattern

```kotlin
class ProductRepository(
    private val db: Database,
    private val cacheManager: CacheManager = CacheManager.global()
) {
    
    fun findByCategory(category: String): List<Product> {
        return cacheManager.executeWithCache(
            db,
            "SELECT * FROM products WHERE category = ?",
            listOf(category),
            ttl = Duration.ofMinutes(30)
        ) { rs ->
            Product(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                category = rs.getString("category"),
                price = rs.getInt("price")
            )
        }
    }
    
    fun update(product: Product) {
        Products.update(db)
            .set(Products.name, product.name)
            .set(Products.price, product.price)
            .where(Products.id, product.id)
            .execute()
        
        // Invalidate cache
        cacheManager.invalidateTable("products")
    }
}
```

### Service Layer

```kotlin
class ProductService(
    private val repository: ProductRepository,
    private val cacheManager: CacheManager
) {
    
    fun getPopularProducts(): List<Product> {
        return cacheManager.executeWithCache(
            db,
            "SELECT * FROM products ORDER BY views DESC LIMIT 10",
            emptyList(),
            ttl = Duration.ofHours(1)
        ) { rs -> repository.mapProduct(rs) }
    }
    
    fun createProduct(product: Product) {
        repository.save(product)
        
        // Clear product caches
        cacheManager.invalidateTable("products")
    }
}
```

---

## Best Practices

### 1. Cache Read-Heavy Queries

**✅ Good - Cache read-heavy queries:**

```kotlin
// Product catalog (rarely changes)
cacheManager.executeWithCache(
    db, "SELECT * FROM products", emptyList(),
    ttl = Duration.ofHours(1)
) { rs -> ... }
```

**❌ Bad - Don't cache volatile data:**

```kotlin
// Real-time stock prices (constantly changing)
cacheManager.executeWithCache(
    db, "SELECT price FROM stocks WHERE symbol = ?", listOf("AAPL")
) { rs -> ... }  // Don't cache this!
```

### 2. Set Appropriate TTL

```kotlin
// Static data - long TTL
val countries = cacheManager.executeWithCache(
    db, "SELECT * FROM countries", emptyList(),
    ttl = Duration.ofDays(1)
) { rs -> ... }

// Semi-static data - medium TTL
val products = cacheManager.executeWithCache(
    db, "SELECT * FROM products", emptyList(),
    ttl = Duration.ofMinutes(30)
) { rs -> ... }

// Dynamic data - short TTL
val prices = cacheManager.executeWithCache(
    db, "SELECT * FROM prices", emptyList(),
    ttl = Duration.ofSeconds(30)
) { rs -> ... }
```

### 3. Invalidate on Writes

```kotlin
fun updateProduct(id: Int, name: String, price: Int) {
    Products.update(db)
        .set(Products.name, name)
        .set(Products.price, price)
        .where(Products.id, id)
        .execute()
    
    // IMPORTANT: Invalidate cache
    cacheManager.invalidateTable("products")
}
```

### 4. Monitor Cache Performance

```kotlin
// Periodic reporting
scheduler.scheduleAtFixedRate({
    val stats = cacheManager.getStats()
    
    if (stats.hitRate < 0.5) {
        logger.warn("Low cache hit rate: ${stats.hitRate}")
    }
    
    if (stats.size > stats.maxSize * 0.9) {
        logger.warn("Cache nearly full: ${stats.size}/${stats.maxSize}")
    }
}, 1, 1, TimeUnit.HOURS)
```

### 5. Use Global Cache Manager

```kotlin
// Configure once at startup
CacheManager.configure(
    cache = InMemoryCache(CacheConfig(maxSize = 10000)),
    config = cacheManagerConfig {
        enableQueryCache = true
        defaultQueryTtl = Duration.ofMinutes(30)
    }
)

// Use everywhere
class UserRepository {
    fun findAll() = CacheManager.global().executeWithCache(...)
}

class ProductRepository {
    fun findAll() = CacheManager.global().executeWithCache(...)
}
```

---

## Performance Tips

### 1. Right-Size Your Cache

```kotlin
// Too small - frequent evictions
CacheConfig(maxSize = 10)  // ❌ Too small

// Just right
CacheConfig(maxSize = 1000)  // ✅ Good

// Too large - memory waste
CacheConfig(maxSize = 1000000)  // ❌ Too large
```

### 2. Use Pattern Invalidation Carefully

```kotlin
// Specific invalidation - fast
cacheManager.invalidate(specificKey)  // ✅ Fast

// Pattern matching - slower
cacheManager.invalidatePattern("query:*products*")  // ⚠️ Slower

// Clear all - slowest
cacheManager.clear()  // ❌ Slowest
```

### 3. Batch Cache Operations

```kotlin
// Bad - multiple cache lookups
products.forEach { product ->
    cacheManager.executeWithCache(...)  // ❌ N lookups
}

// Good - single cache lookup
val allProducts = cacheManager.executeWithCache(...)  // ✅ 1 lookup
products.forEach { product ->
    // Use cached data
}
```

---

## Troubleshooting

### Cache Not Working

**Problem**: Queries not being cached

**Solutions**:
```kotlin
// 1. Check cache is enabled
val config = cacheManagerConfig {
    enableQueryCache = true  // Must be true
}

// 2. Check TTL is set
cacheManager.executeWithCache(
    db, sql, params,
    ttl = Duration.ofMinutes(30)  // Set TTL
) { rs -> ... }

// 3. Check cache size
val stats = cacheManager.getStats()
if (stats.size == 0) {
    // Cache is empty - check configuration
}
```

### Low Hit Rate

**Problem**: Cache hit rate below 50%

**Solutions**:
```kotlin
// 1. Increase cache size
CacheConfig(maxSize = 5000)  // Bigger cache

// 2. Increase TTL
defaultQueryTtl = Duration.ofHours(1)  // Longer TTL

// 3. Check query patterns
val stats = cacheManager.getStats()
println("Hit rate: ${stats.hitRate}")  // Monitor
```

### Memory Usage

**Problem**: Cache using too much memory

**Solutions**:
```kotlin
// 1. Reduce cache size
CacheConfig(maxSize = 500)

// 2. Reduce TTL
defaultQueryTtl = Duration.ofMinutes(5)

// 3. Use aggressive eviction
evictionPolicy = EvictionPolicy.LRU
```

---

## Advanced Features

### Custom Cache Provider

```kotlin
class RedisCache : Cache {
    override fun <T : Any> get(key: String): T? {
        // Redis implementation
    }
    
    override fun <T : Any> put(key: String, value: T, ttl: Duration?) {
        // Redis implementation
    }
    
    // ... implement other methods
}

// Use custom cache
val cacheManager = CacheManager(
    cache = RedisCache()
)
```

### Cache Warming

```kotlin
// Pre-populate cache at startup
fun warmCache(db: Database, cacheManager: CacheManager) {
    val commonQueries = listOf(
        "SELECT * FROM products WHERE featured = true",
        "SELECT * FROM categories ORDER BY name",
        "SELECT * FROM users WHERE active = true"
    )
    
    commonQueries.forEach { sql ->
        cacheManager.executeWithCache(db, sql, emptyList()) { rs ->
            // Warm cache
        }
    }
    
    println("Cache warmed with ${commonQueries.size} queries")
}
```

---

## Next Steps

- **[Performance Optimization](performance.md)** - Advanced optimization techniques
- **[Monitoring Integration](../monitoring/caching.md)** (Coming Soon)
- **[Redis Integration](redis-cache.md)** (Coming Soon)

---

## Complete Example

```kotlin
fun main() {
    // Setup
    val db = Database(/* ... */)
    
    // Configure cache
    CacheManager.configure(
        cache = InMemoryCache(
            CacheConfig(
                maxSize = 5000,
                defaultTtl = Duration.ofMinutes(30),
                evictionPolicy = EvictionPolicy.LRU
            )
        )
    )
    
    val cacheManager = CacheManager.global()
    
    // Query with caching
    val products = cacheManager.executeWithCache(
        db,
        "SELECT * FROM products WHERE category = ?",
        listOf("Electronics"),
        ttl = Duration.ofHours(1)
    ) { rs ->
        Product(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            price = rs.getInt("price")
        )
    }
    
    // Update and invalidate
    Products.update(db)
        .set(Products.price, 999)
        .where(Products.id, 1)
        .execute()
    
    cacheManager.invalidateTable("products")
    
    // Monitor performance
    cacheManager.printStats()
}
```

This example combines:
- ✅ Cache configuration
- ✅ Query caching
- ✅ Cache invalidation
- ✅ Performance monitoring
