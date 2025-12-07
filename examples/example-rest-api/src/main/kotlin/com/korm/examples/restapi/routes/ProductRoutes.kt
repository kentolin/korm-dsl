// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/routes/ProductRoutes.kt

package com.korm.examples.restapi.routes

import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.services.ProductService
import com.korm.examples.restapi.NotFoundException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRoutes(productService: ProductService) {
    route("/products") {
        // Create product
        post {
            val request = call.receive<CreateProductRequest>()
            val product = productService.createProduct(request)
            call.respond(HttpStatusCode.Created, ProductResponse.from(product))
        }

        // Get all products
        get {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
            val activeOnly = call.request.queryParameters["active"]?.toBooleanStrictOrNull() ?: false

            val products = productService.findAll(limit, offset, activeOnly)

            call.respond(
                mapOf(
                    "products" to products.map { ProductResponse.from(it) },
                    "limit" to limit,
                    "offset" to offset
                )
            )
        }

        // Search products
        get("/search") {
            val keyword = call.request.queryParameters["q"]
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Query parameter required")
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

            val products = productService.search(keyword, limit)
            call.respond(products.map { ProductResponse.from(it) })
        }

        // Get product by ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val product = productService.findById(id)
                ?: throw NotFoundException("Product not found")

            call.respond(ProductResponse.from(product))
        }

        // Update product
        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val request = call.receive<UpdateProductRequest>()
            val product = productService.update(id, request)
            call.respond(ProductResponse.from(product))
        }

        // Delete product
        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Invalid ID")

            val deleted = productService.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                throw NotFoundException("Product not found")
            }
        }
    }
}
