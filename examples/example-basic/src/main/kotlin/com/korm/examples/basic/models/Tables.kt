package com.korm.examples.basic.models

import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val age = int("age")
}