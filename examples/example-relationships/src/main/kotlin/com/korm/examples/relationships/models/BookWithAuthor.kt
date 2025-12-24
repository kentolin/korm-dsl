package com.korm.examples.relationships.models

data class BookWithAuthor(
    val bookId: Int,
    val bookTitle: String,
    val isbn: String,
    val publishYear: Int?,
    val price: Double?,
    val authorId: Int,
    val authorName: String,
    val authorEmail: String,
    val authorCountry: String?
)