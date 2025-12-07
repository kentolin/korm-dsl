# KORM Enterprise Example

Production-ready enterprise application demonstrating all KORM DSL features with monitoring, caching, migrations, and comprehensive API.

## Features

### Core Functionality
- ✅ User management with role-based access
- ✅ Product catalog with inventory tracking
- ✅ Order processing with stock management
- ✅ Audit logging for all actions
- ✅ Comprehensive reporting

### Technical Features
- ✅ Database migrations
- ✅ Multi-level caching (in-memory)
- ✅ Connection pooling (HikariCP)
- ✅ Query monitoring and slow query detection
- ✅ Health checks
- ✅ Prometheus metrics
- ✅ Transaction management
- ✅ RESTful API
- ✅ Comprehensive error handling

## Architecture
```
┌─────────────────────────────────────────┐
│           HTTP API (Ktor)               │
├─────────────────────────────────────────┤
│           Service Layer                 │
│  ┌──────┐ ┌─────────┐ ┌──────────┐    │
│  │ User │ │ Product │ │  Order   │    │
│  │Service│ │ Service │ │ Service  │    │
│  └──────┘ └─────────┘ └──────────┘    │
├─────────────────────────────────────────┤
│        KORM DSL Layer                   │
│  ┌──────┐ ┌───────┐ ┌────────────┐    │
│  │ Core │ │ Cache │ │ Monitoring │    │
│  └──────┘ └───────┘ └────────────┘    │
├─────────────────────────────────────────┤
│         PostgreSQL Database             │
└─────────────────────────────────────────┘
```

## Quick Start

### 1. Start Infrastructure
```bash
docker-compose up -d
```

This starts:
- PostgreSQL (port 5432)
- Redis (port 6379)
- Prometheus (port 9090)
- Grafana (port 3000)

### 2. Run Application
```bash
./gradlew :examples:example-enterprise:run
```

Or with sample data:
```bash
./gradlew :examples:example-enterprise:runWithSampleData
```

### 3. Access Services

- **API**: http://localhost:8080
- **Health**: http://localhost:8080/health
- **Metrics**: http://localhost:8080/metrics
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## API Documentation

### Users
```bash
# Create user
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'

# Get user
curl http://localhost:8080/api/users/1

# List users
curl http://localhost:8080/api/users?limit=10&offset=0

# Authenticate
curl -X POST http://localhost:8080/api/users/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### Products
```bash
# Create product
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "Professional Laptop",
    "description": "High-performance laptop",
    "category": "Electronics",
    "price": 1299.99,
    "costPrice": 899.99,
    "stock": 50
  }'

# Get product
curl http://localhost:8080/api/products/1

# Get by SKU
curl http://localhost:8080/api/products/sku/LAPTOP-001

# Get by category
curl http://localhost:8080/api/products/category/Electronics

# Get low stock
curl http://localhost:8080/api/products/low-stock

# Update stock
curl -X PATCH http://localhost:8080/api/products/1/stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 100}'
```

### Orders
```bash
# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 2},
      {"productId": 2, "quantity": 1}
    ],
    "shippingAddress": "123 Main St, City, State 12345",
    "billingAddress": "123 Main St, City, State 12345"
  }'

# Get order
curl http://localhost:8080/api/orders/1

# Get user orders
curl http://localhost:8080/api/orders/user/1

# Update order status
curl -X PATCH http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'

# Cancel order
curl -X POST http://localhost:8080/api/orders/1/cancel
```

### Reports
```bash
# Sales report
curl "http://localhost:8080/api/reports/sales?startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59"

# Top customers
curl http://localhost:8080/api/reports/customers/top?limit=10

# Inventory report
curl http://localhost:8080/api/reports/inventory
```

### Health & Metrics
```bash
# Health check
curl http://localhost:8080/health

# Detailed health
curl http://localhost:8080/health/detailed

# Prometheus metrics
curl http://localhost:8080/metrics

# Database metrics
curl http://localhost:8080/metrics/database

# Query statistics
curl http://localhost:8080/metrics/queries

# Slow queries
curl http://localhost:8080/metrics/queries/slow
```

### Admin
```bash
# Cache stats
curl http://localhost:8080/admin/cache/stats

# Clear cache
curl -X POST http://localhost:8080/admin/cache/clear

# Clear query stats
curl -X POST http://localhost:8080/admin/queries/clear-stats

# System info
curl http://localhost:8080/admin/system
```

## Configuration

Edit `src/main/resources/application.conf`:
```hocon
server {
  port = 8080
  host = "0.0.0.0"
}

database {
  url = "jdbc:postgresql://localhost:5432/korm_enterprise"
  user = "postgres"
  password = "password"
  poolSize = 20
}

cache {
  maxSize = 10000
  ttlMinutes = 30
}

monitoring {
  enabled = true
  slowQueryThresholdMs = 1000
  metricsInterval = 60000
}
```

### Environment Variables

Override configuration with environment variables:

- `PORT` - Server port
- `DB_URL` - Database URL
- `DB_USER` - Database user
- `DB_PASSWORD` - Database password
- `DB_POOL_SIZE` - Connection pool size
- `CACHE_MAX_SIZE` - Maximum cache size
- `CACHE_TTL_MINUTES` - Cache TTL in minutes
- `SLOW_QUERY_THRESHOLD_MS` - Slow query threshold
- `JWT_SECRET` - JWT secret key

## Monitoring with Grafana

1. Open Grafana: http://localhost:3000
2. Login with admin/admin
3. Add Prometheus data source: http://prometheus:9090
4. Import dashboard or create custom panels

### Key Metrics

- `db_queries_total` - Total queries executed
- `db_queries_errors` - Query errors
- `db_queries_duration` - Query duration histogram
- `db_connections_active` - Active connections
- `db_connections_idle` - Idle connections
- `db_transactions_total` - Total transactions
- `db_transactions_commits` - Committed transactions
- `db_transactions_rollbacks` - Rolled back transactions

## Sample Data

Run with sample data to get started quickly:
```bash
./gradlew :examples:example-enterprise:runWithSampleData
```

This creates:
- 5 users (admin, manager, 3 regular users)
- 8 products across different categories
- 3 sample orders with various statuses

### Sample Credentials

- **Admin**: username: `admin`, password: `admin123`
- **Manager**: username: `manager`, password: `manager123`
- **User**: username: `john_doe`, password: `password123`

## Testing

Run integration tests:
```bash
./gradlew :examples:example-enterprise:test
```

## Production Deployment

### Prerequisites

1. PostgreSQL 14+ database
2. Redis (optional, for distributed caching)
3. Prometheus + Grafana (optional, for monitoring)

### Build
```bash
./gradlew :examples:example-enterprise:build
```

### Run
```bash
java -jar build/libs/example-enterprise-0.1.0-SNAPSHOT.jar
```

### Docker Build
```dockerfile
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Best Practices Demonstrated

1. **Separation of Concerns**: Clear layering (API, Service, Data)
2. **Transaction Management**: Proper use of transactions for consistency
3. **Caching Strategy**: Multi-level caching to reduce database load
4. **Error Handling**: Comprehensive error handling and logging
5. **Monitoring**: Built-in metrics and health checks
6. **Security**: Password hashing, audit logging
7. **Testing**: Integration tests with Testcontainers
8. **Configuration**: Externalized configuration with environment variables
9. **Migrations**: Database schema versioning
10. **API Design**: RESTful API with proper HTTP status codes

## Troubleshooting

### Database Connection Issues
```bash
# Check PostgreSQL is running
docker ps | grep postgres

# View logs
docker logs korm-enterprise-db

# Connect to database
docker exec -it korm-enterprise-db psql -U postgres -d korm_enterprise
```

### High Memory Usage

- Reduce cache size: `CACHE_MAX_SIZE=1000`
- Reduce connection pool: `DB_POOL_SIZE=10`
- Increase JVM heap: `-Xmx2g`

### Slow Queries

Check slow query log:
```bash
curl http://localhost:8080/metrics/queries/slow
```

## Contributing

See main repository [CONTRIBUTING.md](../../CONTRIBUTING.md)

## License

MIT License - see [LICENSE](../../LICENSE)
