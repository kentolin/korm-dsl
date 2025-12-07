// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/models/Product.kt

package com.korm.examples.enterprise.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.math.BigDecimal
import java.time.LocalDateTime

data class Product(
    val id: Long = 0,
    val sku: String,
    val name: String,
    val description: String?,
    val category: String,
    val price: BigDecimal,
    val costPrice: BigDecimal,
    val stock: Int,
    val reorderLevel: Int,
    val active: Boolean = true,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

object Products : Table("products") {
    val id = long("id").autoIncrement()
    val sku = varchar("sku", 50).unique().notNull()
    val name = varchar("name", 255).notNull()
    val description = text("description")
    val category = varchar("category", 100).notNull()
    val price = decimal("price", 10, 2).notNull()
    val costPrice = decimal("cost_price", 10, 2).notNull()
    val stock = int("stock").default(0).notNull()
    val reorderLevel = int("reorder_level").default(10).notNull()
    val active = boolean("active").default(true).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_products_sku", listOf("sku"), unique = true)
        index("idx_products_category", listOf("category"))
        index("idx_products_active", listOf("active"))
    }
}
