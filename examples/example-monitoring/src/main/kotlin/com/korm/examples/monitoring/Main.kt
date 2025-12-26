package com.korm.examples.monitoring

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.core.transaction
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.monitoring.*
import com.korm.dsl.query.*
import com.korm.dsl.schema.Table
import com.korm.dsl.schema.create


// Define schema
object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val username = varchar("username", 50).notNull()
    val email = varchar("email", 255).notNull()
}

object Posts : Table("posts") {
    val id = int("id").primaryKey().autoIncrement()
    val userId = int("user_id").notNull()
    val title = varchar("title", 200).notNull()
    val content = varchar("content", 1000).notNull()
}

fun main() {
    println("=== KORM Monitoring Examples ===\n")

    // Example 1: Basic Monitoring Setup
    example1_basicMonitoring()

    // Example 2: Slow Query Detection
    example2_slowQueryDetection()

    // Example 3: Performance Profiling
    example3_performanceProfiler()

    // Example 4: Console Monitor
    example4_consoleMonitor()

    // Example 5: Custom Event Listener
    example5_customListener()

    // Example 6: Production Configuration
    example6_productionConfig()
}

/**
 * Example 1: Basic monitoring setup
 */
fun example1_basicMonitoring() {
    println("\n━━━ Example 1: Basic Monitoring ━━━")

    // Create database
    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:monitor_demo1;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    // Setup monitoring with default configuration
    val monitoring = Monitoring(MonitoringConfig.DEFAULT)

    try {
        // Create schema
        Users.create(db)

        // Execute some queries
        Users.insert(db)
            .set(Users.username, "alice")
            .set(Users.email, "alice@example.com")
            .execute()

        Users.insert(db)
            .set(Users.username, "bob")
            .set(Users.email, "bob@example.com")
            .execute()

        val users = Users.select(db).execute { rs ->
            rs.getString("username")
        }

        println("Inserted and queried ${users.size} users")

        // Get slow query stats (should be none for fast queries)
        val slowStats = monitoring.getSlowQueryStats()
        println("Slow queries detected: ${slowStats?.count ?: 0}")

    } finally {
        monitoring.shutdown()
        db.close()
    }
}

/**
 * Example 2: Slow query detection
 */
fun example2_slowQueryDetection() {
    println("\n━━━ Example 2: Slow Query Detection ━━━")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:monitor_demo2;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    // Setup monitoring with low threshold to simulate slow queries
    val config = monitoringConfig {

        slowQueryThresholdMs = 10  // Very low threshold for demo
        enableSlowQueryLog = true
    }

    val monitoring = Monitoring(config)

    try {
        Users.create(db)

        // Insert data
        repeat(100) { i ->
            Users.insert(db)
                .set(Users.username, "user$i")
                .set(Users.email, "user$i@example.com")
                .execute()
        }

        // This might trigger slow query warnings
        val users = Users.select(db).execute { rs ->
            rs.getString("username")
        }

        println("Queried ${users.size} users")

        // Get slow query statistics
        val stats = monitoring.getSlowQueryStats()
        if (stats != null) {
            println("\nSlow Query Statistics:")
            println("  Count: ${stats.count}")
            println("  Total time: ${stats.totalTimeMs}ms")
            println("  Average time: ${stats.averageTimeMs}ms")
            println("  Threshold: ${stats.thresholdMs}ms")
        }

    } finally {
        monitoring.shutdown()
        db.close()
    }
}

/**
 * Example 3: Performance profiling
 */
fun example3_performanceProfiler() {
    println("\n━━━ Example 3: Performance Profiler ━━━")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:monitor_demo3;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    // Setup monitoring with profiler enabled
    val config = monitoringConfig {
        enablePerformanceProfiler = true
        enableSlowQueryLog = false
        enableConsoleMonitor = false
    }

    val monitoring = Monitoring(config)

    try {
        // Create schema
        Users.create(db)
        Posts.create(db)

        // Execute various operations
        println("Executing various database operations...")

        // Inserts
        repeat(50) { i ->
            Users.insert(db)
                .set(Users.username, "user$i")
                .set(Users.email, "user$i@example.com")
                .execute()
        }

        repeat(100) { i ->
            Posts.insert(db)
                .set(Posts.userId, (i % 50) + 1)
                .set(Posts.title, "Post $i")
                .set(Posts.content, "Content for post $i")
                .execute()
        }

        // Selects
        val users = Users.select(db).execute { rs ->
            rs.getString("username")
        }

        val posts = Posts.select(db)
            .where(Posts.userId, 1)
            .execute { rs ->
                rs.getString("title")
            }

        // Updates
        Users.update(db)
            .set(Users.email, "updated@example.com")
            .where(Users.id, 1)
            .execute()

        // Transaction
        db.transaction {
            Users.update(db)
                .set(Users.username, "alice_updated")
                .where(Users.id, 1)
                .execute()
        }

        println("Executed: ${users.size} user queries, ${posts.size} post queries")

        // Print performance report
        println("\nPerformance Report:")
        monitoring.printPerformanceReport()

    } finally {
        monitoring.shutdown()
        db.close()
    }
}

/**
 * Example 4: Console monitor (real-time output)
 */
fun example4_consoleMonitor() {
    println("\n━━━ Example 4: Console Monitor ━━━")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:monitor_demo4;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    // Setup monitoring with console output
    val config = monitoringConfig {
        enableConsoleMonitor = true
        consoleShowAllQueries = true  // Show all queries
        enablePerformanceProfiler = false
        enableSlowQueryLog = false
    }

    val monitoring = Monitoring(config)

    try {
        println("Watch the console for real-time query monitoring:\n")

        Users.create(db)

        Users.insert(db)
            .set(Users.username, "alice")
            .set(Users.email, "alice@example.com")
            .execute()

        Users.select(db).execute { rs ->
            rs.getString("username")
        }

        Users.update(db)
            .set(Users.email, "alice_new@example.com")
            .where(Users.id, 1)
            .execute()

        db.transaction {
            Users.insert(db)
                .set(Users.username, "bob")
                .set(Users.email, "bob@example.com")
                .execute()
        }

        println("\n✓ All queries logged to console above")

    } finally {
        monitoring.shutdown()
        db.close()
    }
}

/**
 * Example 5: Custom event listener
 */
fun example5_customListener() {
    println("\n━━━ Example 5: Custom Event Listener ━━━")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:monitor_demo5;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    val monitoring = Monitoring(MonitoringConfig.DEFAULT)

    // Create custom listener to track INSERT operations
    val insertCounter = object : QueryEventListener() {
        var insertCount = 0

        override fun handleTypedEvent(event: QueryEvent) {
            if (event.queryType == QueryType.INSERT) {
                insertCount++
                println("  → INSERT #$insertCount: ${event.tableName}")
            }
        }
    }

    // Add custom listener
    monitoring.addListener(insertCounter)

    try {
        Users.create(db)

        println("Tracking INSERT operations:")

        repeat(5) { i ->
            Users.insert(db)
                .set(Users.username, "user$i")
                .set(Users.email, "user$i@example.com")
                .execute()
        }

        println("\nTotal INSERTs tracked: ${insertCounter.insertCount}")

    } finally {
        monitoring.shutdown()
        db.close()
    }
}

/**
 * Example 6: Production-ready configuration
 */
fun example6_productionConfig() {
    println("\n━━━ Example 6: Production Configuration ━━━")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:monitor_demo6;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )
    val db = Database(H2Dialect, pool)

    // Production configuration - minimal overhead
    val config = MonitoringConfig.PRODUCTION

    val monitoring = Monitoring(config)

    try {
        Users.create(db)
        Posts.create(db)

        println("Running with production monitoring config:")
        println("  - Slow query log: ${config.enableSlowQueryLog}")
        println("  - Console monitor: ${config.enableConsoleMonitor}")
        println("  - Performance profiler: ${config.enablePerformanceProfiler}")
        println("  - Slow query threshold: ${config.slowQueryThresholdMs}ms")
        println()

        // Execute operations
        repeat(10) { i ->
            Users.insert(db)
                .set(Users.username, "user$i")
                .set(Users.email, "user$i@example.com")
                .execute()
        }

        val users = Users.select(db).execute { rs ->
            rs.getString("username")
        }

        println("Executed operations with production monitoring")
        println("Found ${users.size} users")

        // Get performance report
        val report = monitoring.getPerformanceReport()
        if (report != null) {
            println("\nQuick Stats:")
            report.queryStats.forEach { stat ->
                println("  ${stat.type}: ${stat.count} queries, avg ${stat.avgTimeMs}ms")
            }
        }

    } finally {
        monitoring.shutdown()
        db.close()
    }
}
