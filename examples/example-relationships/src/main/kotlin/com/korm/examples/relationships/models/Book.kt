package com.korm.examples.relationships.models

data class Book(
    val id: Int,
    val title: String,
    val isbn: String,
    val authorId: Int,
    val publishYear: Int?,
    val price: Double?
)
