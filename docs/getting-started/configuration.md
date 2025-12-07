<!-- korm-dsl/docs/getting-started/configuration.md -->

# Configuration Guide

Learn how to configure KORM DSL for your application.

## Database Connection

### Basic Configuration
```kotlin
val database = Database.connect(
    url = "jdbc:postgresql://localhost:5432/mydb",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "password"
)
```

### Advanced Configuration
```kotlin
val config = HikariConfig().apply {
    jdbcUrl = "jdbc:postgresql://localhost:5432/mydb"
    driverClassName = "org.postgresql.Driver"
    username = "postgres"
    password = "password"
    
    // Connection pool settings
    maximumPoolSize = 20
    minimumIdle = 5
    connectionTimeout = 30000
    idleTimeout = 600000
    maxLifetime = 1800000
    
    // Performance settings
    isAutoCommit = false
    isReadOnly = false
    transactionIsolation = "TRANSACTION_READ_COMMITTED"
}

val dataSource = HikariDataSource(config)
val database = Database.connect(dataSource)
```

## Supported Databases

### PostgreSQL
```kotlin
Database.connect(
    url = "jdbc:postgresql://localhost:5432/mydb",
    driver = "org.postgresql.Driver",
    user = "postgres",
    password = "password"
)
```

### MySQL
```kotlin
Database.connect(
    url = "jdbc:mysql://localhost:3306/mydb",
    driver = "com.mysql.cj.jdbc.Driver",
    user = "root",
    password = "password"
)
```

### SQLite
```kotlin
Database.connect(
    url = "jdbc:sqlite:mydb.db",
    driver = "org.sqlite.JDBC",
    user = "",
    password = ""
)
```

### H2
```kotlin
Database.connect(
    url = "jdbc:h2:mem:test",
    driver = "org.h2.Driver",
    user = "sa",
    password = ""
)
```

## Environment Variables

### Using Environment Variables
```kotlin
val database = Database.connect(
    url = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/mydb",
    driver = System.getenv("DB_DRIVER") ?: "org.postgresql.Driver",
    user = System.getenv("DB_USER") ?: "postgres",
    password = System.getenv("DB_PASSWORD") ?: "password"
)
```

### Configuration File

Create `application.conf`:
```hocon
database {
    url = "jdbc:postgresql://localhost:5432/mydb"
    url = ${?DB_URL}
    
    driver = "org.postgresql.Driver"
    
    user = "postgres"
    user = ${?DB_USER}
    
    password = "password"
    password = ${?DB_PASSWORD}
    
    pool {
        maximumPoolSize = 10
        minimumIdle = 2
    }
}
```

Load configuration:
```kotlin
import com.typesafe.config.ConfigFactory

val config = ConfigFactory.load()
val database = Database.connect(
    url = config.getString("database.url"),
    driver = config.getString("database.driver"),
    user = config.getString("database.user"),
    password = config.getString("database.password")
)
```

## Logging

### SLF4J Configuration

Create `logback.xml`:
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="com.korm" level="DEBUG"/>
    <logger name="com.zaxxer.hikari" level="INFO"/>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### SQL Logging

To log SQL queries:
```kotlin
// Enable SQL logging
database.enableQueryLogging()

// Disable SQL logging
database.disableQueryLogging()
```

## Best Practices

1. **Use Connection Pooling**: Always use a connection pool in production
2. **Set Appropriate Pool Size**: Configure based on your application needs
3. **Use Environment Variables**: Never hardcode credentials
4. **Close Connections**: Always close database connections when done
5. **Use Transactions**: Wrap database operations in transactions
6. **Monitor Performance**: Enable logging and monitoring in production

## Production Configuration

Example production configuration:
```kotlin
val config = HikariConfig().apply {
    jdbcUrl = System.getenv("DB_URL")
    username = System.getenv("DB_USER")
    password = System.getenv("DB_PASSWORD")
    
    maximumPoolSize = 50
    minimumIdle = 10
    connectionTimeout = 30000
    idleTimeout = 600000
    maxLifetime = 1800000
    
    leakDetectionThreshold = 60000
    
    addDataSourceProperty("cachePrepStmts", "true")
    addDataSourceProperty("prepStmtCacheSize", "250")
    addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
}
```
