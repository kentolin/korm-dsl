package com.korm.examples.relationships.models

data class Author(
    val id: Int,
    val name: String,
    val email: String,
    val country: String?
)