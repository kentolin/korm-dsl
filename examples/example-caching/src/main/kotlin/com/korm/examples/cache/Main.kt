package com.korm.examples.cache

import com.korm.dsl.cache.*
import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.query.*
import com.korm.dsl.schema.Table
import com.korm.dsl.schema.create
import java.time.Duration

// Schema definition
object Products : Table("products") {
    val id = int("id").autoIncrement().primaryKey()
    val name = varchar("name", 100).notNull()
    val price = int("price").notNull()
    val category = varchar("category", 50).notNull()
}

fun main() {
    println("=== KORM Caching Examples ===\n")

    // Setup database
    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:cache_demo;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    try {
        // Setup data
        setupData(db)

        // Run examples
        example1_basicCaching(db)
        example2_cacheStats(db)
        example3_customTTL(db)
        example4_cacheInvalidation(db)
        example5_cachePatterns(db)
        example6_performanceComparison(db)

    } finally {
        db.close()
    }
}

/**
 * Setup sample data
 */
fun setupData(db: Database) {
    println("Setting up sample data...\n")

    Products.create(db)

    // Insert products
    val products = listOf(
        Triple("Laptop", 1200, "Electronics"),
        Triple("Phone", 800, "Electronics"),
        Triple("Tablet", 600, "Electronics"),
        Triple("Monitor", 400, "Electronics"),
        Triple("Keyboard", 100, "Electronics"),
        Triple("Mouse", 50, "Electronics"),
        Triple("Desk", 300, "Furniture"),
        Triple("Chair", 200, "Furniture"),
        Triple("Lamp", 80, "Furniture"),
        Triple("Notebook", 5, "Stationery")
    )

    products.forEach { (name, price, category) ->
        Products.insert(db)
            .set(Products.name, name)
            .set(Products.price, price)
            .set(Products.category, category)
            .execute()
    }

    println("✓ Sample data created (${products.size} products)\n")
}

/**
 * Example 1: Basic caching
 */
fun example1_basicCaching(db: Database) {
    println("━━━ Example 1: Basic Caching ━━━\n")

    // Create cache manager
    val cacheManager = CacheManager(
        cache = InMemoryCache(
            CacheConfig(maxSize = 100, defaultTtl = Duration.ofMinutes(5))
        )
    )

    val sql = "SELECT * FROM products WHERE category = ?"
    val params = listOf("Electronics")

    // First query - cache miss
    println("1. First query (cache miss):")
    val start1 = System.currentTimeMillis()
    val result1 = cacheManager.executeWithCache(db, sql, params) { rs ->
        rs.getString("name")
    }
    val time1 = System.currentTimeMillis() - start1
    println("   Found ${result1.size} products in ${time1}ms")

    // Second query - cache hit
    println("\n2. Second query (cache hit):")
    val start2 = System.currentTimeMillis()
    val result2 = cacheManager.executeWithCache(db, sql, params) { rs ->
        rs.getString("name")
    }
    val time2 = System.currentTimeMillis() - start2
    println("   Found ${result2.size} products in ${time2}ms")

    println("\n   Cache speedup: ${time1 / maxOf(time2, 1)}x faster")
    println()
}

/**
 * Example 2: Cache statistics
 */
fun example2_cacheStats(db: Database) {
    println("━━━ Example 2: Cache Statistics ━━━\n")

    val cacheManager = CacheManager(
        cache = InMemoryCache(CacheConfig(maxSize = 50))
    )

    // Execute multiple queries
    repeat(10) { i ->
        cacheManager.executeWithCache(
            db,
            "SELECT * FROM products WHERE price > ?",
            listOf(i * 100)
        ) { rs ->
            rs.getString("name")
        }
    }

    // Execute same queries again (cache hits)
    repeat(10) { i ->
        cacheManager.executeWithCache(
            db,
            "SELECT * FROM products WHERE price > ?",
            listOf(i * 100)
        ) { rs ->
            rs.getString("name")
        }
    }

    // Print statistics
    cacheManager.printStats()
}

/**
 * Example 3: Custom TTL
 */
fun example3_customTTL(db: Database) {
    println("\n━━━ Example 3: Custom TTL ━━━\n")

    val cacheManager = CacheManager()

    // Cache with 2 second TTL
    println("1. Caching with 2-second TTL:")
    cacheManager.executeWithCache(
        db,
        "SELECT * FROM products",
        emptyList(),
        ttl = Duration.ofSeconds(2)
    ) { rs ->
        rs.getString("name")
    }
    println("   ✓ Cached")

    // Immediate re-query (cache hit)
    println("\n2. Immediate re-query (cache hit):")
    val start1 = System.currentTimeMillis()
    cacheManager.executeWithCache(
        db,
        "SELECT * FROM products",
        emptyList(),
        ttl = Duration.ofSeconds(2)
    ) { rs ->
        rs.getString("name")
    }
    val time1 = System.currentTimeMillis() - start1
    println("   Retrieved in ${time1}ms (from cache)")

    // Wait for expiration
    println("\n3. Waiting 3 seconds for cache to expire...")
    Thread.sleep(3000)

    // Query after expiration (cache miss)
    println("\n4. Query after expiration (cache miss):")
    val start2 = System.currentTimeMillis()
    cacheManager.executeWithCache(
        db,
        "SELECT * FROM products",
        emptyList(),
        ttl = Duration.ofSeconds(2)
    ) { rs ->
        rs.getString("name")
    }
    val time2 = System.currentTimeMillis() - start2
    println("   Retrieved in ${time2}ms (from database)")
    println()
}

/**
 * Example 4: Cache invalidation
 */
fun example4_cacheInvalidation(db: Database) {
    println("━━━ Example 4: Cache Invalidation ━━━\n")

    val cacheManager = CacheManager()

    // Cache a query
    println("1. Caching product query:")
    cacheManager.executeWithCache(
        db,
        "SELECT * FROM products WHERE category = ?",
        listOf("Electronics")
    ) { rs ->
        rs.getString("name")
    }
    println("   ✓ Cached")

    val stats1 = cacheManager.getStats()
    println("   Cache size: ${stats1.size}")

    // Update database
    println("\n2. Updating products...")
    Products.update(db)
        .set(Products.price, 999)
        .where(Products.name, "Laptop")
        .execute()
    println("   ✓ Updated")

    // Invalidate cache
    println("\n3. Invalidating cache for 'products' table:")
    cacheManager.invalidateTable("products")

    val stats2 = cacheManager.getStats()
    println("   Cache size after invalidation: ${stats2.size}")

    // Query again (cache miss)
    println("\n4. Querying again (cache miss):")
    val result = cacheManager.executeWithCache(
        db,
        "SELECT * FROM products WHERE category = ?",
        listOf("Electronics")
    ) { rs ->
        rs.getString("name")
    }
    println("   Found ${result.size} products (fresh data)")
    println()
}

/**
 * Example 5: Cache patterns
 */
fun example5_cachePatterns(db: Database) {
    println("━━━ Example 5: Cache Patterns ━━━\n")

    val cache = InMemoryCache(CacheConfig(maxSize = 100))

    // Add some cache entries
    cache.put("user:1", "Alice")
    cache.put("user:2", "Bob")
    cache.put("user:3", "Carol")
    cache.put("product:1", "Laptop")
    cache.put("product:2", "Phone")
    cache.put("order:1", "Order #1")

    println("1. Cache entries added:")
    println("   user:1, user:2, user:3")
    println("   product:1, product:2")
    println("   order:1")

    // Remove user entries by pattern
    println("\n2. Removing all 'user:*' entries:")
    cache.removePattern("user:*")

    println("   user:1 exists: ${cache.contains("user:1")} ✗")
    println("   product:1 exists: ${cache.contains("product:1")} ✓")
    println("   order:1 exists: ${cache.contains("order:1")} ✓")
    println()
}

/**
 * Example 6: Performance comparison
 */
fun example6_performanceComparison(db: Database) {
    println("━━━ Example 6: Performance Comparison ━━━\n")

    val cacheManager = CacheManager()
    val iterations = 100

    val sql = "SELECT * FROM products WHERE price > ?"
    val params = listOf(100)

    // Without caching - using raw SQL
    println("1. Without caching ($iterations iterations):")
    val start1 = System.currentTimeMillis()
    repeat(iterations) {
        db.useConnection { conn ->
            conn.prepareStatement("SELECT * FROM products WHERE price > ?").use { stmt ->
                stmt.setInt(1, 100)
                val rs = stmt.executeQuery()
                while (rs.next()) {
                    rs.getString("name")
                }
            }
        }
    }
    val time1 = System.currentTimeMillis() - start1
    println("   Total time: ${time1}ms")
    println("   Avg per query: ${time1 / iterations}ms")

    // With caching
    println("\n2. With caching ($iterations iterations):")
    val start2 = System.currentTimeMillis()
    repeat(iterations) {
        cacheManager.executeWithCache(db, sql, params) { rs ->
            rs.getString("name")
        }
    }
    val time2 = System.currentTimeMillis() - start2
    println("   Total time: ${time2}ms")
    println("   Avg per query: ${time2 / iterations}ms")

    // Cache statistics
    val stats = cacheManager.getStats()
    println("\n3. Cache performance:")
    println("   Hits: ${stats.hits}")
    println("   Misses: ${stats.misses}")
    println("   Hit rate: ${String.format("%.2f%%", stats.hitRate * 100)}")
    println("   Speedup: ${time1 / maxOf(time2, 1)}x faster")

    println("\n✓ Caching examples completed!")
}
