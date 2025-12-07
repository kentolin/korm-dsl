// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/models/Book.kt

package com.korm.examples.relationships.models

data class Book(
    val id: Long = 0,
    val title: String,
    val isbn: String,
    val authorId: Long,
    val publishedYear: Int,
    val pages: Int? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class BookWithAuthor(
    val book: Book,
    val author: Author
)

data class BookWithCategories(
    val book: Book,
    val categories: List<Category>
)
