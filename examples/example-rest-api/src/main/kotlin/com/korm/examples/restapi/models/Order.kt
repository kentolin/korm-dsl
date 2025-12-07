// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/models/Order.kt

package com.korm.examples.restapi.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val id: Long = 0,
    val userId: Long,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}

object Orders : Table("orders") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").notNull()
    val status = varchar("status", 20).notNull()
    val totalAmount = decimal("total_amount", 10, 2).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(userId, Users, Users.id)
    }
}

data class OrderItem(
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal
)

object OrderItems : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").notNull()
    val productId = long("product_id").notNull()
    val quantity = int("quantity").notNull()
    val price = decimal("price", 10, 2).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(orderId, Orders, Orders.id)
        foreignKey(productId, Products, Products.id)
    }
}

data class CreateOrderRequest(
    val userId: Long,
    val items: List<OrderItemRequest>
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

data class OrderResponse(
    val id: Long,
    val userId: Long,
    val status: String,
    val totalAmount: String,
    val items: List<OrderItemResponse>,
    val createdAt: String,
    val updatedAt: String
)

data class OrderItemResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: String
)
