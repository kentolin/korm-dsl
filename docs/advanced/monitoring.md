# Monitoring & Profiling

Complete guide to monitoring and profiling database operations with KORM's comprehensive monitoring system.

---

## Overview

KORM provides a powerful event-based monitoring system that allows you to:

- 📊 **Track Query Performance** - Monitor execution time and identify bottlenecks
- 🐌 **Detect Slow Queries** - Automatically log queries exceeding thresholds
- 📈 **Profile Performance** - Collect detailed statistics and metrics
- 🔍 **Real-time Monitoring** - Watch queries execute in real-time
- 📝 **Custom Listeners** - Implement your own monitoring logic
- ⚡ **Low Overhead** - Minimal performance impact in production

---

## Quick Start

### Basic Setup

```kotlin
import com.korm.dsl.monitoring.*

// Use default configuration
val monitoring = Monitoring(MonitoringConfig.DEFAULT)

// Your database operations...
Users.insert(db).set(Users.name, "Alice").execute()

// Get performance report
monitoring.printPerformanceReport()
```

### Development Mode

```kotlin
// Full monitoring with console output
val monitoring = Monitoring(MonitoringConfig.DEVELOPMENT)

// Queries will be logged to console as they execute
Users.select(db).execute { /* ... */ }
```

### Production Mode

```kotlin
// Minimal overhead, only slow queries
val monitoring = Monitoring(MonitoringConfig.PRODUCTION)
```

---

## Configuration

### Pre-defined Configurations

```kotlin
// Default - balanced monitoring
MonitoringConfig.DEFAULT

// Development - verbose output
MonitoringConfig.DEVELOPMENT

// Production - minimal overhead
MonitoringConfig.PRODUCTION

// Disabled - no monitoring
MonitoringConfig.DISABLED
```

### Custom Configuration

```kotlin
val config = monitoringConfig {
    enableQueryMonitoring = true
    enableSlowQueryLog = true
    slowQueryThresholdMs = 1000
    enableConsoleMonitor = true
    consoleShowAllQueries = false
    enablePerformanceProfiler = true
}

val monitoring = Monitoring(config)
```

### Configuration Options

| Option | Description | Default |
|--------|-------------|---------|
| `enableQueryMonitoring` | Track all queries | `true` |
| `enableSlowQueryLog` | Log slow queries | `true` |
| `slowQueryThresholdMs` | Slow query threshold (ms) | `1000` |
| `enableConsoleMonitor` | Real-time console output | `false` |
| `consoleShowAllQueries` | Show all queries in console | `false` |
| `enablePerformanceProfiler` | Collect performance metrics | `true` |
| `enableConnectionMonitoring` | Monitor connection pool | `true` |
| `enableTransactionMonitoring` | Monitor transactions | `true` |
| `enableBatchMonitoring` | Monitor batch operations | `true` |

---

## Monitoring Events

### Query Events

Emitted for every SQL query:

```kotlin
QueryEvent(
    sql = "SELECT * FROM users WHERE id = ?",
    parameters = listOf(1),
    executionTimeMs = 45,
    rowsAffected = 1,
    success = true,
    queryType = QueryType.SELECT,
    tableName = "users"
)
```

### Transaction Events

```kotlin
TransactionEvent(
    action = TransactionAction.COMMIT,
    durationMs = 150,
    success = true
)
```

### Connection Events

```kotlin
ConnectionEvent(
    eventName = "connection_acquired",
    activeConnections = 5,
    idleConnections = 10,
    totalConnections = 15
)
```

### Batch Events

```kotlin
BatchEvent(
    operation = "INSERT",
    batchSize = 100,
    totalItems = 1000,
    executionTimeMs = 250,
    itemsPerSecond = 4000.0
)
```

### Migration Events

```kotlin
MigrationEvent(
    version = 1,
    description = "Create users table",
    action = MigrationAction.UP,
    executionTimeMs = 50,
    success = true
)
```

---

## Slow Query Logging

### Automatic Detection

Queries exceeding the threshold are automatically logged:

```kotlin
val config = monitoringConfig {
    enableSlowQueryLog = true
    slowQueryThresholdMs = 1000  // 1 second
}

val monitoring = Monitoring(config)

// This slow query will be logged
Thread.sleep(1500)  // Simulate slow query
```

### Slow Query Log Output

```
⚠️ SLOW QUERY DETECTED
  Time: 2024-12-25T10:15:23.456Z
  Execution: 1523ms (threshold: 1000ms)
  Type: SELECT
  Table: users
  Rows: 150
  SQL: SELECT * FROM users WHERE created_at > ?
  Parameters: [2024-01-01]
```

### Get Statistics

```kotlin
val stats = monitoring.getSlowQueryStats()

println("Slow queries: ${stats.count}")
println("Total time: ${stats.totalTimeMs}ms")
println("Average time: ${stats.averageTimeMs}ms")
```

---

## Performance Profiling

### Collect Metrics

The performance profiler automatically collects detailed metrics:

```kotlin
val monitoring = Monitoring(MonitoringConfig.DEFAULT)

// Execute various operations...
Users.insert(db).set(Users.name, "Alice").execute()
Users.select(db).execute { /* ... */ }

// Get detailed report
val report = monitoring.getPerformanceReport()
```

### Performance Report

```kotlin
monitoring.printPerformanceReport()
```

**Output:**

```
╔════════════════════════════════════════════════════════════════╗
║              KORM Performance Report                          ║
╚════════════════════════════════════════════════════════════════╝

Duration: 2m 15s

─── Query Statistics ───────────────────────────────────────────
  SELECT       │ Count: 450      │ Avg: 25ms │ Min: 5ms │ Max: 150ms │ Errors: 0
  INSERT       │ Count: 200      │ Avg: 15ms │ Min: 8ms │ Max: 45ms  │ Errors: 0
  UPDATE       │ Count: 50       │ Avg: 20ms │ Min: 10ms │ Max: 35ms │ Errors: 0
  DELETE       │ Count: 10       │ Avg: 18ms │ Min: 12ms │ Max: 25ms │ Errors: 0

─── Table Statistics ───────────────────────────────────────────
  users                │ SEL: 250   │ INS: 100   │ UPD: 30    │ DEL: 5     │ Time: 5250ms
  posts                │ SEL: 200   │ INS: 100   │ UPD: 20    │ DEL: 5     │ Time: 4800ms

─── Connection Pool ────────────────────────────────────────────
  Peak Active Connections: 8
  Peak Pending Threads: 2
  Total Acquisitions: 710

─── Transactions ───────────────────────────────────────────────
  Commits: 25
  Rollbacks: 1
  Avg Duration: 45ms

─── Batch Operations ───────────────────────────────────────────
  Operations: 5
  Total Items: 5000
  Avg Items/Operation: 1000
  Throughput: 3333.33 items/s

════════════════════════════════════════════════════════════════
```

### Query by Type

```kotlin
val report = monitoring.getPerformanceReport()

report.queryStats.forEach { stat ->
    println("${stat.type}: ${stat.count} queries")
    println("  Avg time: ${stat.avgTimeMs}ms")
    println("  Min/Max: ${stat.minTimeMs}ms / ${stat.maxTimeMs}ms")
    println("  Errors: ${stat.errorCount}")
}
```

### Query by Table

```kotlin
report.tableStats.forEach { stat ->
    println("Table: ${stat.tableName}")
    println("  SELECTs: ${stat.selectCount}")
    println("  INSERTs: ${stat.insertCount}")
    println("  UPDATEs: ${stat.updateCount}")
    println("  DELETEs: ${stat.deleteCount}")
    println("  Total time: ${stat.totalTimeMs}ms")
}
```

---

## Console Monitor

### Real-Time Query Logging

```kotlin
val config = monitoringConfig {
    enableConsoleMonitor = true
    consoleShowAllQueries = true
}

val monitoring = Monitoring(config)

// Queries will appear in console as they execute
Users.select(db).execute { /* ... */ }
```

### Console Output

```
✓ [10:15:23.456] SELECT users - 25ms
  SQL: SELECT * FROM users WHERE id = ?
  Params: [1]

✓ [10:15:23.482] INSERT users - 15ms

⚠ [10:15:25.123] SELECT posts - 1250ms  (SLOW)
  SQL: SELECT * FROM posts WHERE user_id IN (SELECT id FROM users)
```

### Color-Coded Output

- ✓ **Green** - Successful query
- ⚠ **Yellow** - Slow query
- ✗ **Red** - Failed query
- **Blue** - SELECT queries
- **Green** - INSERT queries
- **Yellow** - UPDATE queries
- **Red** - DELETE queries

---

## Custom Event Listeners

### Create Custom Listener

```kotlin
class QueryCountListener : QueryEventListener() {
    var selectCount = 0
    var insertCount = 0
    
    override fun handleTypedEvent(event: QueryEvent) {
        when (event.queryType) {
            QueryType.SELECT -> selectCount++
            QueryType.INSERT -> insertCount++
            else -> {}
        }
    }
}

// Use the listener
val listener = QueryCountListener()
monitoring.addListener(listener)

// After queries...
println("SELECTs: ${listener.selectCount}")
println("INSERTs: ${listener.insertCount}")
```

### Typed Listeners

```kotlin
// Listen only to query events
class MyQueryListener : QueryEventListener() {
    override fun handleTypedEvent(event: QueryEvent) {
        // Handle query events
    }
}

// Listen only to transaction events
class MyTransactionListener : TransactionEventListener() {
    override fun handleTypedEvent(event: TransactionEvent) {
        // Handle transaction events
    }
}

// Listen only to connection events
class MyConnectionListener : ConnectionEventListener() {
    override fun handleTypedEvent(event: ConnectionEvent) {
        // Handle connection events
    }
}
```

### Generic Listener

```kotlin
class MyGenericListener : EventListener {
    override fun onEvent(event: MonitoringEvent) {
        when (event) {
            is QueryEvent -> handleQuery(event)
            is TransactionEvent -> handleTransaction(event)
            is ConnectionEvent -> handleConnection(event)
            is BatchEvent -> handleBatch(event)
            is MigrationEvent -> handleMigration(event)
        }
    }
    
    private fun handleQuery(event: QueryEvent) { /* ... */ }
    private fun handleTransaction(event: TransactionEvent) { /* ... */ }
    private fun handleConnection(event: ConnectionEvent) { /* ... */ }
    private fun handleBatch(event: BatchEvent) { /* ... */ }
    private fun handleMigration(event: MigrationEvent) { /* ... */ }
}
```

---

## Integration Examples

### With Logging Framework

```kotlin
class SLF4JMonitorListener : EventListener {
    private val logger = LoggerFactory.getLogger("korm.monitor")
    
    override fun onEvent(event: MonitoringEvent) {
        when (event) {
            is QueryEvent -> {
                if (event.executionTimeMs > 100) {
                    logger.warn("Slow query: ${event.sql} - ${event.executionTimeMs}ms")
                } else {
                    logger.debug("Query: ${event.sql} - ${event.executionTimeMs}ms")
                }
            }
            is TransactionEvent -> {
                logger.info("Transaction ${event.action}: ${event.durationMs}ms")
            }
            else -> {}
        }
    }
}
```

### With Metrics Library (Micrometer)

```kotlin
class MicrometerMonitor(private val registry: MeterRegistry) : EventListener {
    
    private val queryTimer = registry.timer("korm.queries")
    private val queryCounter = registry.counter("korm.queries.count")
    
    override fun onEvent(event: MonitoringEvent) {
        if (event is QueryEvent) {
            queryTimer.record(event.executionTimeMs, TimeUnit.MILLISECONDS)
            queryCounter.increment()
            
            registry.counter("korm.queries.by_type", "type", event.queryType.name)
                .increment()
        }
    }
}
```

### With APM Tools

```kotlin
class DatadogMonitor : EventListener {
    override fun onEvent(event: MonitoringEvent) {
        if (event is QueryEvent) {
            // Send to Datadog
            statsd.recordExecutionTime(
                "database.query.time",
                event.executionTimeMs,
                "query_type:${event.queryType}",
                "table:${event.tableName}"
            )
        }
    }
}
```

---

## Best Practices

### 1. Choose Right Configuration for Environment

```kotlin
// Development
val devMonitoring = Monitoring(MonitoringConfig.DEVELOPMENT)

// Production
val prodMonitoring = Monitoring(MonitoringConfig.PRODUCTION)
```

### 2. Set Appropriate Slow Query Threshold

```kotlin
val config = monitoringConfig {
    // For OLTP systems
    slowQueryThresholdMs = 100
    
    // For analytics/reporting
    slowQueryThresholdMs = 5000
}
```

### 3. Monitor in Production Without Performance Impact

```kotlin
val config = monitoringConfig {
    enableSlowQueryLog = true          // Log slow queries
    enablePerformanceProfiler = true   // Collect metrics
    enableConsoleMonitor = false       // Don't spam console
    consoleShowAllQueries = false
    slowQueryThresholdMs = 1000
}
```

### 4. Periodic Reporting

```kotlin
// Schedule periodic reports
val executor = Executors.newScheduledThreadPool(1)

executor.scheduleAtFixedRate({
    monitoring.printPerformanceReport()
    monitoring.reset()  // Reset stats after reporting
}, 1, 1, TimeUnit.HOURS)
```

### 5. Alert on Anomalies

```kotlin
class AlertingListener : QueryEventListener() {
    override fun handleTypedEvent(event: QueryEvent) {
        if (event.executionTimeMs > 5000) {
            // Send alert - very slow query!
            sendAlert("Very slow query detected: ${event.sql}")
        }
        
        if (!event.success) {
            // Send alert - query failed!
            sendAlert("Query failed: ${event.exception?.message}")
        }
    }
}
```

---

## Performance Impact

### Overhead Measurements

| Configuration | Overhead | Use Case |
|---------------|----------|----------|
| Disabled | 0% | Testing only |
| Production | <1% | Production systems |
| Default | 1-2% | Most environments |
| Development | 2-5% | Development only |

### Optimization Tips

1. **Disable Console Monitor in Production**
   ```kotlin
   enableConsoleMonitor = false
   ```

2. **Use Appropriate Threshold**
   ```kotlin
   slowQueryThresholdMs = 1000  // Only log truly slow queries
   ```

3. **Limit Custom Listeners**
    - Keep listener logic simple
    - Avoid blocking operations
    - Don't throw exceptions

4. **Reset Statistics Periodically**
   ```kotlin
   monitoring.reset()  // Prevent memory growth
   ```

---

## Troubleshooting

### High Memory Usage

**Problem**: Performance profiler consuming too much memory

**Solution**: Reset statistics periodically
```kotlin
// Reset every hour
monitoring.reset()
```

### Missing Events

**Problem**: Events not being captured

**Solution**: Ensure monitoring is configured before database operations
```kotlin
// Setup monitoring FIRST
val monitoring = Monitoring(config)

// Then execute queries
Users.select(db).execute { /* ... */ }
```

### Slow Query Threshold Not Working

**Problem**: Slow queries not being logged

**Solution**: Check threshold and ensure slow query log is enabled
```kotlin
val config = monitoringConfig {
    enableSlowQueryLog = true  // Must be true
    slowQueryThresholdMs = 1000
}
```

---

## Advanced Features

### Multiple Event Buses

```kotlin
// Create separate event bus for specific monitoring
val customBus = EventBus()
val customMonitor = QueryMonitor(customBus)

// Add listeners to custom bus
customBus.addListener(MyCustomListener())
```

### Conditional Monitoring

```kotlin
class ConditionalListener : QueryEventListener() {
    override fun handleTypedEvent(event: QueryEvent) {
        // Only monitor specific tables
        if (event.tableName in listOf("users", "orders")) {
            logEvent(event)
        }
    }
}
```

### Query Pattern Detection

```kotlin
class QueryPatternDetector : QueryEventListener() {
    private val patterns = mutableMapOf<String, Int>()
    
    override fun handleTypedEvent(event: QueryEvent) {
        // Normalize SQL to detect patterns
        val pattern = normalizeSQL(event.sql)
        patterns[pattern] = (patterns[pattern] ?: 0) + 1
    }
    
    fun getTopPatterns(limit: Int = 10): List<Pair<String, Int>> {
        return patterns.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key to it.value }
    }
}
```

---

## Next Steps

- **[Performance Optimization](performance.md)** - Optimize query performance
- **[Testing](../testing/monitoring.md)** (Coming Soon) - Test monitoring setup
- **[Production Deployment](../deployment/monitoring.md)** (Coming Soon) - Production monitoring

---

## Complete Example

```kotlin
fun main() {
    // Setup monitoring
    val config = monitoringConfig {
        enableSlowQueryLog = true
        slowQueryThresholdMs = 500
        enablePerformanceProfiler = true
        enableConsoleMonitor = true
        consoleShowAllQueries = false
    }
    
    val monitoring = Monitoring(config)
    
    // Add custom listener
    val alertListener = object : QueryEventListener() {
        override fun handleTypedEvent(event: QueryEvent) {
            if (event.executionTimeMs > 2000) {
                println("⚠️ ALERT: Very slow query detected!")
            }
        }
    }
    monitoring.addListener(alertListener)
    
    try {
        // Your database operations...
        val db = Database(/* ... */)
        
        Users.insert(db).set(Users.name, "Alice").execute()
        Users.select(db).execute { /* ... */ }
        
        // Print report
        monitoring.printPerformanceReport()
        
        // Get statistics
        val slowStats = monitoring.getSlowQueryStats()
        println("Slow queries: ${slowStats?.count ?: 0}")
        
    } finally {
        monitoring.shutdown()
    }
}
```
