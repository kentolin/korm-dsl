# KORM DSL - Cache Module

High-performance caching for KORM DSL.

## Features

- **Multiple Cache Providers**: In-Memory, Caffeine, Redis
- **Entity Caching**: Cache database entities by ID
- **Query Caching**: Cache query results
- **Eviction Strategies**: LRU, LFU, FIFO
- **TTL Support**: Per-entry time-to-live
- **Statistics**: Hit rate, miss rate, eviction count
- **Multi-Level Caching**: L1 and L2 cache support

## Usage

### In-Memory Cache
```kotlin
val cache = InMemoryCache<Long, User>(
    config = CacheConfig(
        maxSize = 10000,
        defaultTTL = Duration.ofMinutes(10),
        evictionStrategy = EvictionStrategyType.LRU
    )
)

// Store and retrieve
cache.put(1L, user)
val cached = cache.get(1L)
```

### Entity Cache
```kotlin
val entityCache = EntityCache<Long, User>(
    entityClass = User::class,
    config = CacheConfig(maxSize = 1000)
)

// Get or load
val user = entityCache.getOrLoad(1L) { id ->
    userRepository.findById(id)
}
```

### Query Cache
```kotlin
val queryCache = QueryCache<User>()

val users = queryCache.executeAndCache(
    query = "SELECT * FROM users WHERE age > ?",
    params = mapOf("age" to 18),
    ttl = Duration.ofMinutes(5)
) {
    database.select(Users) { 
        where(Users.age gt 18) 
    }
}
```

### Redis Cache
```kotlin
val redisCache = RedisCache<String, User>(
    host = "localhost",
    port = 6379,
    serializer = JsonCacheSerializer(User::class.java)
)

redisCache.put("user:1", user, Duration.ofHours(1))
val cached = redisCache.get("user:1")
```

### Cache Manager
```kotlin
val userCache = GlobalCacheManager.getCache<Long, User>("users")
val productCache = GlobalCacheManager.getCache<Long, Product>("products")

// Clear all caches
GlobalCacheManager.clearAll()

// Get statistics
val stats = GlobalCacheManager.getAllStats()
```

## See Also

- [Caching Guide](../../docs/advanced/caching.md)
- [Examples](../../examples/)
