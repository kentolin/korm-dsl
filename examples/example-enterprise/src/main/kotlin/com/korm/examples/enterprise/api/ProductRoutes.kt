// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/api/ProductRoutes.kt

package com.korm.examples.enterprise.api

import com.korm.examples.enterprise.services.AuditService
import com.korm.examples.enterprise.services.ProductService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.math.BigDecimal

fun Route.productRoutes(productService: ProductService, auditService: AuditService) {
    route("/api/products") {
        // Create product
        post {
            val request = call.receive<CreateProductRequest>()

            val product = productService.createProduct(
                sku = request.sku,
                name = request.name,
                description = request.description,
                category = request.category,
                price = request.price,
                costPrice = request.costPrice,
                stock = request.stock,
                reorderLevel = request.reorderLevel ?: 10
            )

            auditService.logAction(
                userId = null,
                action = "CREATE_PRODUCT",
                entityType = "Product",
                entityId = product.id,
                newValue = "sku=${product.sku}, name=${product.name}"
            )

            call.respond(HttpStatusCode.Created, product)
        }

        // Get product by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

            val product = productService.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))

            call.respond(product)
        }

        // Get by SKU
        get("/sku/{sku}") {
            val sku = call.parameters["sku"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "SKU required"))

            val product = productService.findBySku(sku)
                ?: return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "Product not found"))

            call.respond(product)
        }

        // Get by category
        get("/category/{category}") {
            val category = call.parameters["category"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Category required"))
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50

            val products = productService.findByCategory(category, limit)
            call.respond(products)
        }

        // Get low stock products
        get("/low-stock") {
            val products = productService.findLowStock()
            call.respond(products)
        }

        // Update stock
        patch("/{id}/stock") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid ID"))

            val request = call.receive<UpdateStockRequest>()

            val product = if (request.adjustment != null) {
                productService.adjustStock(id, request.adjustment)
            } else if (request.quantity != null) {
                productService.updateStock(id, request.quantity)
            } else {
                return@patch call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Either quantity or adjustment required")
                )
            }

            auditService.logAction(
                userId = null,
                action = "UPDATE_STOCK",
                entityType = "Product",
                entityId = id,
                newValue = "stock=${product.stock}"
            )

            call.respond(product)
        }
    }
}

data class CreateProductRequest(
    val sku: String,
    val name: String,
    val description: String?,
    val category: String,
    val price: BigDecimal,
    val costPrice: BigDecimal,
    val stock: Int,
    val reorderLevel: Int? = null
)

data class UpdateStockRequest(
    val quantity: Int? = null,
    val adjustment: Int? = null
)
