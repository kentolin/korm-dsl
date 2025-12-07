// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/models/Tables.kt

package com.korm.examples.relationships.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table

object Authors : Table("authors") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).unique().notNull()
    val bio = text("bio")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)
}

object Books : Table("books") {
    val id = long("id").autoIncrement()
    val title = varchar("title", 255).notNull()
    val isbn = varchar("isbn", 20).unique().notNull()
    val authorId = long("author_id").notNull()
    val publishedYear = int("published_year").notNull()
    val pages = int("pages")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        foreignKey(authorId, Authors, Authors.id)
    }
}

object Categories : Table("categories") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).unique().notNull()
    val description = text("description")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)
}

object BookCategories : Table("book_categories") {
    val bookId = long("book_id").notNull()
    val categoryId = long("category_id").notNull()

    override val primaryKey = PrimaryKey(bookId, categoryId)

    init {
        foreignKey(bookId, Books, Books.id)
        foreignKey(categoryId, Categories, Categories.id)
    }
}
