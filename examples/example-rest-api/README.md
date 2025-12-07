# korm-dsl/examples/example-rest-api/README.md

# KORM REST API Example

Complete REST API example using KORM DSL with Ktor.

## Features

- User management (CRUD)
- Product catalog
- Order processing
- Transaction management
- Error handling
- Health checks

## Running

### Start Database
```bash
docker-compose up -d
```

### Run Application
```bash
./gradlew :examples:example-rest-api:run
```

The API will be available at `http://localhost:8080`

## API Endpoints

### Users

- `POST /api/users` - Create user
- `GET /api/users` - List users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Products

- `POST /api/products` - Create product
- `GET /api/products` - List products
- `GET /api/products/search?q={keyword}` - Search products
- `GET /api/products/{id}` - Get product by ID
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product

### Orders

- `POST /api/orders` - Create order
- `GET /api/orders/{id}` - Get order by ID
- `GET /api/orders/user/{userId}` - Get user orders
- `PATCH /api/orders/{id}/status` - Update order status
- `POST /api/orders/{id}/cancel` - Cancel order

### Health

- `GET /api/health` - Health check

## Examples

### Create User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Create Product
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 999.99,
    "stock": 50
  }'
```

### Create Order
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'
```
