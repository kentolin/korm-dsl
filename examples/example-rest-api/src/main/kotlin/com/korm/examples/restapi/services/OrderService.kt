// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/services/OrderService.kt

package com.korm.examples.restapi.services

import com.korm.dsl.core.Database
import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.NotFoundException
import com.korm.examples.restapi.ValidationException
import java.math.BigDecimal
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class OrderService(
    private val database: Database
) {
    private val productService = ProductService(database)

    fun createOrder(request: CreateOrderRequest): OrderResponse {
        if (request.items.isEmpty()) {
            throw ValidationException("Order must have at least one item")
        }

        return database.transaction {
            // Calculate total and validate products
            var totalAmount = BigDecimal.ZERO
            val orderItemsData = mutableListOf<Triple<Long, Int, BigDecimal>>()

            request.items.forEach { item ->
                val product = productService.findById(item.productId)
                    ?: throw NotFoundException("Product ${item.productId} not found")

                if (!product.active) {
                    throw ValidationException("Product ${product.name} is not available")
                }

                if (product.stock < item.quantity) {
                    throw ValidationException("Insufficient stock for ${product.name}")
                }

                val itemTotal = product.price.multiply(BigDecimal(item.quantity))
                totalAmount = totalAmount.add(itemTotal)

                orderItemsData.add(Triple(item.productId, item.quantity, product.price))
            }

            // Create order
            val orderId = insertInto(Orders) {
                it[Orders.userId] = request.userId
                it[Orders.status] = OrderStatus.PENDING.name
                it[Orders.totalAmount] = totalAmount
            }.let { insert(it) }

            // Create order items
            orderItemsData.forEach { (productId, quantity, price) ->
                insertInto(OrderItems) {
                    it[OrderItems.orderId] = orderId
                    it[OrderItems.productId] = productId
                    it[OrderItems.quantity] = quantity
                    it[OrderItems.price] = price
                }.let { insert(it) }

                // Update product stock
                val product = productService.findById(productId)!!
                productService.updateStock(productId, product.stock - quantity)
            }

            findById(orderId)!!
        }
    }

    fun findById(id: Long): OrderResponse? {
        return database.transaction {
            val orderQuery = from(Orders).where(Orders.id eq id)
            val orders = select(orderQuery, ::mapOrder)
            val order = orders.firstOrNull() ?: return@transaction null

            // Get order items
            val itemsQuery = """
                SELECT oi.*, p.name as product_name
                FROM order_items oi
                JOIN products p ON oi.product_id = p.id
                WHERE oi.order_id = ?
            """.trimIndent()

            val items = mutableListOf<OrderItemResponse>()
            getConnection().use { conn ->
                conn.prepareStatement(itemsQuery).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            items.add(
                                OrderItemResponse(
                                    id = rs.getLong("id"),
                                    productId = rs.getLong("product_id"),
                                    productName = rs.getString("product_name"),
                                    quantity = rs.getInt("quantity"),
                                    price = rs.getBigDecimal("price").toString()
                                )
                            )
                        }
                    }
                }
            }

            OrderResponse(
                id = order.id,
                userId = order.userId,
                status = order.status.name,
                totalAmount = order.totalAmount.toString(),
                items = items,
                createdAt = order.createdAt.toString(),
                updatedAt = order.updatedAt.toString()
            )
        }
    }

    fun findByUserId(userId: Long, limit: Int = 50): List<OrderResponse> {
        return database.transaction {
            val query = from(Orders)
                .where(Orders.userId eq userId)
                .orderBy(Orders.createdAt, "DESC")
                .limit(limit)

            val orders = select(query, ::mapOrder)
            orders.mapNotNull { order -> findById(order.id) }
        }
    }

    fun updateStatus(id: Long, status: OrderStatus): OrderResponse {
        database.transaction {
            val query = update(Orders) {
                it[Orders.status] = status.name
                it[Orders.updatedAt] = System.currentTimeMillis()
            }.where(Orders.id eq id)

            update(query)
        }

        return findById(id) ?: throw NotFoundException("Order not found")
    }

    fun cancelOrder(id: Long): OrderResponse {
        return database.transaction {
            val order = findById(id) ?: throw NotFoundException("Order not found")

            if (order.status != OrderStatus.PENDING.name) {
                throw ValidationException("Can only cancel pending orders")
            }

            // Restore product stock
            order.items.forEach { item ->
                val product = productService.findById(item.productId)!!
                productService.updateStock(item.productId, product.stock + item.quantity)
            }

            updateStatus(id, OrderStatus.CANCELLED)
        }
    }

    private fun mapOrder(rs: ResultSet): Order {
        return Order(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id"),
            status = OrderStatus.valueOf(rs.getString("status")),
            totalAmount = rs.getBigDecimal("total_amount"),
            createdAt = toLocalDateTime(rs.getLong("created_at")),
            updatedAt = toLocalDateTime(rs.getLong("updated_at"))
        )
    }

    private fun toLocalDateTime(epochMilli: Long): LocalDateTime {
        return LocalDateTime.ofInstant(
            Instant.ofEpochMilli(epochMilli),
            ZoneId.systemDefault()
        )
    }
}
