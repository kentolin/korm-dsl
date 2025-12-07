// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/models/Order.kt

package com.korm.examples.enterprise.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.math.BigDecimal
import java.time.LocalDateTime

data class Order(
    val id: Long = 0,
    val orderNumber: String,
    val userId: Long,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val taxAmount: BigDecimal,
    val discountAmount: BigDecimal,
    val shippingAddress: String?,
    val billingAddress: String?,
    val notes: String? = null,
    val placedAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}

object Orders : Table("orders") {
    val id = long("id").autoIncrement()
    val orderNumber = varchar("order_number", 50).unique().notNull()
    val userId = long("user_id").notNull()
    val status = varchar("status", 20).notNull()
    val totalAmount = decimal("total_amount", 10, 2).notNull()
    val taxAmount = decimal("tax_amount", 10, 2).default(BigDecimal.ZERO).notNull()
    val discountAmount = decimal("discount_amount", 10, 2).default(BigDecimal.ZERO).notNull()
    val shippingAddress = text("shipping_address")
    val billingAddress = text("billing_address")
    val notes = text("notes")
    val placedAt = timestamp("placed_at").default(System.currentTimeMillis()).notNull()
    val completedAt = timestamp("completed_at")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(userId, Users, Users.id)
        index("idx_orders_order_number", listOf("order_number"), unique = true)
        index("idx_orders_user_id", listOf("user_id"))
        index("idx_orders_status", listOf("status"))
        index("idx_orders_placed_at", listOf("placed_at"))
    }
}

data class OrderItem(
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discount: BigDecimal = BigDecimal.ZERO,
    val totalPrice: BigDecimal
)

object OrderItems : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").notNull()
    val productId = long("product_id").notNull()
    val quantity = int("quantity").notNull()
    val unitPrice = decimal("unit_price", 10, 2).notNull()
    val discount = decimal("discount", 10, 2).default(BigDecimal.ZERO).notNull()
    val totalPrice = decimal("total_price", 10, 2).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(orderId, Orders, Orders.id)
        foreignKey(productId, Products, Products.id)
        index("idx_order_items_order_id", listOf("order_id"))
        index("idx_order_items_product_id", listOf("product_id"))
    }
}
