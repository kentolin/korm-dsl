// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/data/SampleDataLoader.kt

package com.korm.examples.enterprise.data

import com.korm.dsl.core.Database
import com.korm.examples.enterprise.models.UserRole
import com.korm.examples.enterprise.services.*
import org.slf4j.LoggerFactory
import java.math.BigDecimal

class SampleDataLoader(
    private val database: Database,
    private val userService: UserService,
    private val productService: ProductService,
    private val orderService: OrderService
) {
    private val logger = LoggerFactory.getLogger(SampleDataLoader::class.java)

    fun loadSampleData() {
        logger.info("Loading sample data...")

        try {
            // Create users
            logger.info("Creating sample users...")
            val admin = userService.createUser(
                username = "admin",
                email = "admin@example.com",
                password = "admin123",
                firstName = "System",
                lastName = "Administrator",
                role = UserRole.ADMIN
            )

            val manager = userService.createUser(
                username = "manager",
                email = "manager@example.com",
                password = "manager123",
                firstName = "Store",
                lastName = "Manager",
                role = UserRole.MANAGER
            )

            val users = listOf(
                userService.createUser(
                    username = "john_doe",
                    email = "john@example.com",
                    password = "password123",
                    firstName = "John",
                    lastName = "Doe"
                ),
                userService.createUser(
                    username = "jane_smith",
                    email = "jane@example.com",
                    password = "password123",
                    firstName = "Jane",
                    lastName = "Smith"
                ),
                userService.createUser(
                    username = "bob_wilson",
                    email = "bob@example.com",
                    password = "password123",
                    firstName = "Bob",
                    lastName = "Wilson"
                )
            )

            logger.info("Created ${users.size + 2} users")

            // Create products
            logger.info("Creating sample products...")
            val products = listOf(
                productService.createProduct(
                    sku = "LAPTOP-001",
                    name = "Professional Laptop",
                    description = "High-performance laptop for professionals",
                    category = "Electronics",
                    price = BigDecimal("1299.99"),
                    costPrice = BigDecimal("899.99"),
                    stock = 50,
                    reorderLevel = 10
                ),
                productService.createProduct(
                    sku = "MOUSE-001",
                    name = "Wireless Mouse",
                    description = "Ergonomic wireless mouse",
                    category = "Electronics",
                    price = BigDecimal("49.99"),
                    costPrice = BigDecimal("25.00"),
                    stock = 200,
                    reorderLevel = 50
                ),
                productService.createProduct(
                    sku = "KEYBOARD-001",
                    name = "Mechanical Keyboard",
                    description = "RGB mechanical gaming keyboard",
                    category = "Electronics",
                    price = BigDecimal("149.99"),
                    costPrice = BigDecimal("89.99"),
                    stock = 100,
                    reorderLevel = 20
                ),
                productService.createProduct(
                    sku = "MONITOR-001",
                    name = "4K Monitor",
                    description = "27-inch 4K IPS monitor",
                    category = "Electronics",
                    price = BigDecimal("599.99"),
                    costPrice = BigDecimal("399.99"),
                    stock = 30,
                    reorderLevel = 5
                ),
                productService.createProduct(
                    sku = "DESK-001",
                    name = "Standing Desk",
                    description = "Adjustable height standing desk",
                    category = "Furniture",
                    price = BigDecimal("449.99"),
                    costPrice = BigDecimal("299.99"),
                    stock = 25,
                    reorderLevel = 5
                ),
                productService.createProduct(
                    sku = "CHAIR-001",
                    name = "Ergonomic Chair",
                    description = "Premium ergonomic office chair",
                    category = "Furniture",
                    price = BigDecimal("399.99"),
                    costPrice = BigDecimal("249.99"),
                    stock = 40,
                    reorderLevel = 10
                ),
                productService.createProduct(
                    sku = "HEADSET-001",
                    name = "Wireless Headset",
                    description = "Noise-cancelling wireless headset",
                    category = "Electronics",
                    price = BigDecimal("199.99"),
                    costPrice = BigDecimal("129.99"),
                    stock = 75,
                    reorderLevel = 15
                ),
                productService.createProduct(
                    sku = "WEBCAM-001",
                    name = "HD Webcam",
                    description = "1080p HD webcam with auto-focus",
                    category = "Electronics",
                    price = BigDecimal("89.99"),
                    costPrice = BigDecimal("59.99"),
                    stock = 60,
                    reorderLevel = 20
                )
            )

            logger.info("Created ${products.size} products")

            // Create sample orders
            logger.info("Creating sample orders...")
            val orders = mutableListOf<com.korm.examples.enterprise.services.OrderWithItems>()

            // Order 1: John's laptop bundle
            orders.add(
                orderService.createOrder(
                    OrderService.CreateOrderRequest(
                        userId = users[0].id,
                        items = listOf(
                            OrderService.OrderItemRequest(products[0].id, 1), // Laptop
                            OrderService.OrderItemRequest(products[1].id, 1), // Mouse
                            OrderService.OrderItemRequest(products[2].id, 1)  // Keyboard
                        ),
                        shippingAddress = "123 Main St, City, State 12345",
                        billingAddress = "123 Main St, City, State 12345"
                    )
                )
            )

            // Order 2: Jane's home office setup
            orders.add(
                orderService.createOrder(
                    OrderService.CreateOrderRequest(
                        userId = users[1].id,
                        items = listOf(
                            OrderService.OrderItemRequest(products[3].id, 1), // Monitor
                            OrderService.OrderItemRequest(products[4].id, 1), // Desk
                            OrderService.OrderItemRequest(products[5].id, 1)  // Chair
                        ),
                        shippingAddress = "456 Oak Ave, Town, State 67890",
                        billingAddress = "456 Oak Ave, Town, State 67890"
                    )
                )
            )

            // Order 3: Bob's accessories
            orders.add(
                orderService.createOrder(
                    OrderService.CreateOrderRequest(
                        userId = users[2].id,
                        items = listOf(
                            OrderService.OrderItemRequest(products[6].id, 1), // Headset
                            OrderService.OrderItemRequest(products[7].id, 1), // Webcam
                            OrderService.OrderItemRequest(products[1].id, 2)  // 2x Mouse
                        ),
                        shippingAddress = "789 Pine Rd, Village, State 11111",
                        billingAddress = "789 Pine Rd, Village, State 11111"
                    )
                )
            )

            logger.info("Created ${orders.size} sample orders")

            // Update some order statuses
            orderService.updateStatus(orders[0].order.id, com.korm.examples.enterprise.models.OrderStatus.CONFIRMED)
            orderService.updateStatus(orders[1].order.id, com.korm.examples.enterprise.models.OrderStatus.SHIPPED)
            orderService.updateStatus(orders[2].order.id, com.korm.examples.enterprise.models.OrderStatus.DELIVERED)

            logger.info("Sample data loaded successfully!")
            logger.info("Admin credentials - username: admin, password: admin123")
            logger.info("Manager credentials - username: manager, password: manager123")
            logger.info("User credentials - username: john_doe, password: password123")

        } catch (e: Exception) {
            logger.error("Failed to load sample data", e)
            throw e
        }
    }
}
