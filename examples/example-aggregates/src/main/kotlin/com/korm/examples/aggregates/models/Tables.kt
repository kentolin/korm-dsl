package com.korm.examples.aggregates.models

import com.korm.dsl.schema.Table

object Products : Table("products") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val category = varchar("category", 50).notNull()
    val price = double("price").notNull()
    val stock = int("stock").notNull()
}

object Orders : Table("orders") {
    val id = int("id").primaryKey().autoIncrement()
    val customerId = int("customer_id").notNull()
    val productId = int("product_id").notNull().references(Products.id)
    val quantity = int("quantity").notNull()
    val totalAmount = double("total_amount").notNull()
    val orderDate = varchar("order_date", 20).notNull()
    val status = varchar("status", 20).notNull()
}

object Customers : Table("customers") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val city = varchar("city", 50).notNull()
    val country = varchar("country", 50).notNull()
}