// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/services/OrderService.kt

package com.korm.examples.enterprise.services

import com.korm.dsl.core.Database
import com.korm.dsl.monitoring.DatabaseMetrics
import com.korm.dsl.monitoring.QueryMonitor
import com.korm.examples.enterprise.models.*
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class OrderService(
    private val database: Database,
    private val productService: ProductService,
    private val metrics: DatabaseMetrics,
    private val queryMonitor: QueryMonitor
) {

    data class CreateOrderRequest(
        val userId: Long,
        val items: List<OrderItemRequest>,
        val shippingAddress: String?,
        val billingAddress: String?,
        val notes: String? = null
    )

    data class OrderItemRequest(
        val productId: Long,
        val quantity: Int,
        val discount: BigDecimal = BigDecimal.ZERO
    )

    fun createOrder(request: CreateOrderRequest): OrderWithItems {
        if (request.items.isEmpty()) {
            throw IllegalArgumentException("Order must have at least one item")
        }

        val startTime = System.nanoTime()

        val order = database.transaction {
            // Generate order number
            val orderNumber = generateOrderNumber()

            // Calculate totals
            var subtotal = BigDecimal.ZERO
            var totalDiscount = BigDecimal.ZERO
            val orderItemsData = mutableListOf<Triple<Long, Int, BigDecimal>>()

            request.items.forEach { item ->
                val product = productService.findById(item.productId)
                    ?: throw IllegalArgumentException("Product ${item.productId} not found")

                if (!product.active) {
                    throw IllegalArgumentException("Product ${product.name} is not available")
                }

                if (product.stock < item.quantity) {
                    throw IllegalArgumentException("Insufficient stock for ${product.name}")
                }

                val itemPrice = product.price.multiply(BigDecimal(item.quantity))
                val itemDiscount = item.discount.multiply(BigDecimal(item.quantity))
                val itemTotal = itemPrice.subtract(itemDiscount)

                subtotal = subtotal.add(itemPrice)
                totalDiscount = totalDiscount.add(itemDiscount)

                orderItemsData.add(Triple(item.productId, item.quantity, product.price))
            }

            val taxRate = BigDecimal("0.10") // 10% tax
            val taxableAmount = subtotal.subtract(totalDiscount)
            val taxAmount = taxableAmount.multiply(taxRate).setScale(2, BigDecimal.ROUND_HALF_UP)
            val totalAmount = taxableAmount.add(taxAmount)

            // Create order
            val orderId = insertInto(Orders) {
                it[Orders.orderNumber] = orderNumber
                it[Orders.userId] = request.userId
                it[Orders.status] = OrderStatus.PENDING.name
                it[Orders.totalAmount] = totalAmount
                it[Orders.taxAmount] = taxAmount
                it[Orders.discountAmount] = totalDiscount
                request.shippingAddress?.let { addr -> it[Orders.shippingAddress] = addr }
                request.billingAddress?.let { addr -> it[Orders.billingAddress] = addr }
                request.notes?.let { n -> it[Orders.notes] = n }
            }.let { insert(it) }

            // Create order items and update stock
            orderItemsData.forEach { (productId, quantity, unitPrice) ->
                val discount = request.items.find { it.productId == productId }?.discount ?: BigDecimal.ZERO
                val itemTotal = unitPrice.multiply(BigDecimal(quantity)).subtract(
                    discount.multiply(BigDecimal(quantity))
                )

                insertInto(OrderItems) {
                    it[OrderItems.orderId] = orderId
                    it[OrderItems.productId] = productId
                    it[OrderItems.quantity] = quantity
                    it[OrderItems.unitPrice] = unitPrice
                    it[OrderItems.discount] = discount
                    it[OrderItems.totalPrice] = itemTotal
                }.let { insert(it) }

                // Adjust stock
                productService.adjustStock(productId, -quantity)
            }

            findById(orderId)!!
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordInsert()
        queryMonitor.recordQuery("CREATE ORDER with items", emptyMap(), duration)

        return order
    }

    fun findById(id: Long): OrderWithItems? {
        val startTime = System.nanoTime()

        val orderWithItems = database.transaction {
            // Get order
            val orderQuery = from(Orders).where(Orders.id eq id)
            val orders = select(orderQuery, ::mapOrder)
            val order = orders.firstOrNull() ?: return@transaction null

            // Get order items
            val itemsSql = """
                SELECT oi.*, p.name as product_name, p.sku as product_sku
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
            """.trimIndent()

            val items = mutableListOf<OrderItemWithProduct>()
            getConnection().use { conn ->
                conn.prepareStatement(itemsSql).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            items.add(mapOrderItemWithProduct(rs))
                        }
                    }
                }
            }

            OrderWithItems(order, items)
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordSelect()
        queryMonitor.recordQuery("SELECT order with items WHERE id = ?", mapOf("id" to id), duration)

        return orderWithItems
    }

    fun findByUserId(userId: Long, limit: Int = 50): List<OrderWithItems> {
        val startTime = System.nanoTime()

        val orders = database.transaction {
            val query = from(Orders)
                .where(Orders.userId eq userId)
                .orderBy(Orders.placedAt, "DESC")
                .limit(limit)

            select(query, ::mapOrder).mapNotNull { order ->
                findById(order.id)
            }
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        queryMonitor.recordQuery("SELECT orders by user WHERE userId = ?", mapOf("userId" to userId), duration)

        return orders
    }

    fun updateStatus(id: Long, status: OrderStatus): Order {
        val startTime = System.nanoTime()

        database.transaction {
            val updates = mutableMapOf<String, Any>()
            updates["status"] = status.name

            if (status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED) {
                updates["completedAt"] = System.currentTimeMillis()
            }

            update(Orders) {
                it[Orders.status] = status.name
                if (status == OrderStatus.DELIVERED) {
                    it[Orders.completedAt] = System.currentTimeMillis()
                }
                it[Orders.updatedAt] = System.currentTimeMillis()
            }.where(Orders.id eq id).let { update(it) }
        }

        val duration = (System.nanoTime() - startTime) / 1_000_000
        metrics.recordUpdate()
        queryMonitor.recordQuery("UPDATE order status WHERE id = ?", mapOf("id" to id), duration)

        return findById(id)?.order ?: throw IllegalStateException("Order not found after update")
    }

    fun cancelOrder(id: Long): Order {
        val orderWithItems = findById(id) ?: throw IllegalArgumentException("Order not found")

        if (orderWithItems.order.status !in listOf(OrderStatus.PENDING, OrderStatus.CONFIRMED)) {
            throw IllegalArgumentException("Can only cancel pending or confirmed orders")
        }

        database.transaction {
            // Restore stock
            orderWithItems.items.forEach { item ->
                productService.adjustStock(item.productId, item.quantity)
            }

            // Update order status
            update(Orders) {
                it[Orders.status] = OrderStatus.CANCELLED.name
                it[Orders.updatedAt] = System.currentTimeMillis()
            }.where(Orders.id eq id).let { update(it) }
        }

        return findById(id)?.order ?: throw IllegalStateException("Order not found after cancellation")
    }

    fun getOrdersByStatus(status: OrderStatus, limit: Int = 100): List<OrderWithItems> {
        return database.transaction {
            val query = from(Orders)
                .where(Orders.status eq status.name)
                .orderBy(Orders.placedAt, "DESC")
                .limit(limit)

            select(query, ::mapOrder).mapNotNull { order ->
                findById(order.id)
            }
        }
    }

    private fun generateOrderNumber(): String {
        val timestamp = System.currentTimeMillis()
        val random = UUID.randomUUID().toString().substring(0, 8).uppercase()
        return "ORD-$timestamp-$random"
    }

    private fun mapOrder(rs: ResultSet): Order {
        return Order(
            id = rs.getLong("id"),
            orderNumber = rs.getString("order_number"),
            userId = rs.getLong("user_id"),
            status = OrderStatus.valueOf(rs.getString("status")),
            totalAmount = rs.getBigDecimal("total_amount"),
            taxAmount = rs.getBigDecimal("tax_amount"),
            discountAmount = rs.getBigDecimal("discount_amount"),
            shippingAddress = rs.getString("shipping_address"),
            billingAddress = rs.getString("billing_address"),
            notes = rs.getString("notes"),
            placedAt = toLocalDateTime(rs.getLong("placed_at")),
            completedAt = rs.getLong("completed_at").takeIf { it > 0 }?.let { toLocalDateTime(it) },
            createdAt = toLocalDateTime(rs.getLong("created_at")),
            updatedAt = toLocalDateTime(rs.getLong("updated_at"))
        )
    }

    private fun mapOrderItemWithProduct(rs: ResultSet): OrderItemWithProduct {
        return OrderItemWithProduct(
            id = rs.getLong("id"),
            orderId = rs.getLong("order_id"),
            productId = rs.getLong("product_id"),
            productName = rs.getString("product_name"),
            productSku = rs.getString("product_sku"),
            quantity = rs.getInt("quantity"),
            unitPrice = rs.getBigDecimal("unit_price"),
            discount = rs.getBigDecimal("discount"),
            totalPrice = rs.getBigDecimal("total_price")
        )
    }

    private fun toLocalDateTime(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            ZoneId.systemDefault()
        )
    }
}

data class OrderWithItems(
    val order: Order,
    val items: List<OrderItemWithProduct>
)

data class OrderItemWithProduct(
    val id: Long,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productSku: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discount: BigDecimal,
    val totalPrice: BigDecimal
)
