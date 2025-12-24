package com.korm.examples.relationships.models

import com.korm.dsl.schema.Table

object Authors : Table("authors") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).notNull().unique()
    val country = varchar("country", 100)
}

object Books : Table("books") {
    val id = int("id").primaryKey().autoIncrement()
    val title = varchar("title", 200).notNull()
    val isbn = varchar("isbn", 20).notNull().unique()
    val authorId = int("author_id").notNull().references(Authors.id)
    val publishYear = int("publish_year")
    val price = double("price")
}

object Categories : Table("categories") {
    val id = int("id").primaryKey().autoIncrement()
    val name = varchar("name", 100).notNull().unique()
    val description = text("description")
}

object BookCategories : Table("book_categories") {
    val bookId = int("book_id").notNull().references(Books.id)
    val categoryId = int("category_id").notNull().references(Categories.id)
}