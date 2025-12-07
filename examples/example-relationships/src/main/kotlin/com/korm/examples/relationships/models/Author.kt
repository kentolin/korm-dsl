// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/models/Author.kt

package com.korm.examples.relationships.models

data class Author(
    val id: Long = 0,
    val name: String,
    val email: String,
    val bio: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthorWithBooks(
    val author: Author,
    val books: List<Book>
)
