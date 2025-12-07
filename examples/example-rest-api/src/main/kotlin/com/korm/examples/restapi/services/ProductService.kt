// korm-dsl/examples/example-rest-api/src/main/kotlin/com/korm/examples/restapi/services/ProductService.kt

package com.korm.examples.restapi.services

import com.korm.dsl.core.Database
import com.korm.examples.restapi.models.*
import com.korm.examples.restapi.NotFoundException
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ProductService(private val database: Database) {

    fun createProduct(request: CreateProductRequest): Product {
        val productId = database.transaction {
            val query = insertInto(Products) {
                it[Products.name] = request.name
                request.description?.let { desc -> it[Products.description] = desc }
                it[Products.price] = request.price
                it[Products.stock] = request.stock
            }
            insert(query)
        }

        return findById(productId) ?: throw IllegalStateException("Failed to create product")
    }

    fun findById(id: Long): Product? {
        return database.transaction {
            val query = from(Products).where(Products.id eq id)
            val results = select(query, ::mapProduct)
            results.firstOrNull()
        }
    }

    fun findAll(
        limit: Int = 100,
        offset: Int = 0,
        activeOnly: Boolean = false
    ): List<Product> {
        return database.transaction {
            var query = from(Products)

            if (activeOnly) {
                query = query.where(Products.active eq true)
            }

            query = query.orderBy(Products.name)
                .limit(limit)
                .offset(offset)

            select(query, ::mapProduct)
        }
    }

    fun search(keyword: String, limit: Int = 20): List<Product> {
        return database.transaction {
            val query = from(Products)
                .where(Products.name like "%$keyword%")
                .orderBy(Products.name)
                .limit(limit)

            select(query, ::mapProduct)
        }
    }

    fun update(id: Long, request: UpdateProductRequest): Product {
        database.transaction {
            val query = update(Products) {
                request.name?.let { name -> it[Products.name] = name }
                request.description?.let { desc -> it[Products.description] = desc }
                request.price?.let { price -> it[Products.price] = price }
                request.stock?.let { stock -> it[Products.stock] = stock }
                request.active?.let { active -> it[Products.active] = active }
                it[Products.updatedAt] = System.currentTimeMillis()
            }.where(Products.id eq id)

            update(query)
        }

        return findById(id) ?: throw NotFoundException("Product not found")
    }

    fun updateStock(id: Long, quantity: Int): Product {
        database.transaction {
            val query = update(Products) {
                it[Products.stock] = quantity
                it[Products.updatedAt] = System.currentTimeMillis()
            }.where(Products.id eq id)

            update(query)
        }

        return findById(id) ?: throw NotFoundException("Product not found")
    }

    fun delete(id: Long): Boolean {
        val deleted = database.transaction {
            val query = deleteFrom(Products) {
                where(Products.id eq id)
            }
            delete(query)
        }

        return deleted > 0
    }

    private fun mapProduct(rs: ResultSet): Product {
        return Product(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            price = rs.getBigDecimal("price"),
            stock = rs.getInt("stock"),
            active = rs.getBoolean("active"),
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
