// korm-dsl/examples/example-enterprise/src/test/kotlin/com/korm/examples/enterprise/EnterpriseIntegrationTest.kt

package com.korm.examples.enterprise

import com.korm.dsl.cache.CacheConfig
import com.korm.dsl.cache.InMemoryCache
import com.korm.dsl.core.Database
import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.examples.enterprise.models.*
import com.korm.examples.enterprise.services.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThan
import org.testcontainers.containers.PostgreSQLContainer
import java.math.BigDecimal
import java.time.Duration

class EnterpriseIntegrationTest : FunSpec({

    val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
        withDatabaseName("test_db")
        withUsername("test")
        withPassword("test")
        start()
    }

    val database = Database.connect(
        url = postgres.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgres.username,
        password = postgres.password
    )

    val cache = InMemoryCache<String, Any>(
        config = CacheConfig(
            maxSize = 1000,
            defaultTTL = Duration.ofMinutes(10)
        )
    )

    val metrics = DatabaseMetrics()
    val queryMonitor = QueryMonitor()

    val userService = UserService(database, cache, metrics, queryMonitor)
    val productService = ProductService(database, cache, metrics, queryMonitor)
    val orderService = OrderService(database, productService, metrics, queryMonitor)
    val reportingService = ReportingService(database, queryMonitor)
    val auditService = AuditService(database)

    beforeSpec {
        // Create tables
        database.createTables(Users, Products, Orders, OrderItems, AuditLogs)
    }

    afterSpec {
        database.close()
        postgres.stop()
    }

    test("should create and retrieve user") {
        val user = userService.createUser(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            firstName = "Test",
            lastName = "User"
        )

        user.id shouldBeGreaterThan 0
        user.username shouldBe "testuser"

        val retrieved = userService.findById(user.id)
        retrieved shouldNotBe null
        retrieved?.username shouldBe "testuser"
    }

    test("should create and retrieve product") {
        val product = productService.createProduct(
            sku = "TEST-001",
            name = "Test Product",
            description = "Test description",
            category = "Test",
            price = BigDecimal("99.99"),
            costPrice = BigDecimal("49.99"),
            stock = 100
        )

        product.id shouldBeGreaterThan 0
        product.sku shouldBe "TEST-001"

        val retrieved = productService.findById(product.id)
        retrieved shouldNotBe null
        retrieved?.name shouldBe "Test Product"
    }

    test("should create order and update stock") {
        val user = userService.createUser(
            username = "orderuser",
            email = "order@example.com",
            password = "password123",
            firstName = "Order",
            lastName = "User"
        )

        val product = productService.createProduct(
            sku = "ORDER-001",
            name = "Order Product",
            description = null,
            category = "Test",
            price = BigDecimal("50.00"),
            costPrice = BigDecimal("25.00"),
            stock = 100
        )

        val initialStock = product.stock

        val order = orderService.createOrder(
            OrderService.CreateOrderRequest(
                userId = user.id,
                items = listOf(
                    OrderService.OrderItemRequest(product.id, 5)
                ),
                shippingAddress = "123 Test St",
                billingAddress = "123 Test St"
            )
        )

        order.order.id shouldBeGreaterThan 0
        order.items shouldHaveSize 1

        val updatedProduct = productService.findById(product.id)
        updatedProduct?.stock shouldBe (initialStock - 5)
    }

    test("should track audit logs") {
        val user = userService.createUser(
            username = "audituser",
            email = "audit@example.com",
            password = "password123",
            firstName = "Audit",
            lastName = "User"
        )

        auditService.logAction(
            userId = user.id,
            action = "TEST_ACTION",
            entityType = "User",
            entityId = user.id,
            newValue = "test value"
        )

        val logs = auditService.findByUser(user.id)
        logs shouldHaveSize 1
        logs[0].action shouldBe "TEST_ACTION"
    }

    test("should generate sales report") {
        val user = userService.createUser(
            username = "reportuser",
            email = "report@example.com",
            password = "password123",
            firstName = "Report",
            lastName = "User"
        )

        val product = productService.createProduct(
            sku = "REPORT-001",
            name = "Report Product",
            description = null,
            category = "Test",
            price = BigDecimal("100.00"),
            costPrice = BigDecimal("50.00"),
            stock = 100
        )

        orderService.createOrder(
            OrderService.CreateOrderRequest(
                userId = user.id,
                items = listOf(
                    OrderService.OrderItemRequest(product.id, 2)
                ),
                shippingAddress = "123 Test St",
                billingAddress = "123 Test St"
            )
        )

        val startDate = System.currentTimeMillis() - 86400000 // 1 day ago
        val endDate = System.currentTimeMillis()

        val report = reportingService.generateSalesReport(startDate, endDate)

        report.totalOrders shouldBeGreaterThan 0
        report.totalRevenue shouldBeGreaterThan BigDecimal.ZERO
    }

    test("should cache user lookups") {
        val user = userService.createUser(
            username = "cacheuser",
            email = "cache@example.com",
            password = "password123",
            firstName = "Cache",
            lastName = "User"
        )

        // First lookup - from database
        val retrieved1 = userService.findById(user.id)

        // Second lookup - from cache
        val retrieved2 = userService.findById(user.id)

        retrieved1 shouldNotBe null
        retrieved2 shouldNotBe null
        retrieved1?.id shouldBe retrieved2?.id

        val cacheStats = cache.stats()
        cacheStats.hitCount shouldBeGreaterThan 0
    }

    test("should monitor slow queries") {
        val initialCount = queryMonitor.getSlowQueries().size

        // This should be logged as a slow query (threshold is 1000ms)
        Thread.sleep(1100)

        queryMonitor.recordQuery(
            sql = "SELECT * FROM users",
            parameters = emptyMap(),
            durationMs = 1500
        )

        val slowQueries = queryMonitor.getSlowQueries()
        slowQueries.size shouldBeGreaterThan initialCount
    }
})
