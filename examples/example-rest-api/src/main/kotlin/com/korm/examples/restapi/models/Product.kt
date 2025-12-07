// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/models/Product.kt

package com.korm.examples.restapi.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.math.BigDecimal
import java.time.LocalDateTime

data class Product(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stock: Int,
    val active: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

object Products : Table("products") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 255).notNull()
    val description = text("description")
    val price = decimal("price", 10, 2).notNull()
    val stock = int("stock").default(0).notNull()
    val active = boolean("active").default(true).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)
}

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stock: Int
)

data class UpdateProductRequest(
    val name: String? = null,
    val description: String? = null,
    val price: BigDecimal? = null,
    val stock: Int? = null,
    val active: Boolean? = null
)

data class ProductResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: String,
    val stock: Int,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(product: Product): ProductResponse {
            return ProductResponse(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price.toString(),
                stock = product.stock,
                active = product.active,
                createdAt = product.createdAt.toString(),
                updatedAt = product.updatedAt.toString()
            )
        }
    }
}
