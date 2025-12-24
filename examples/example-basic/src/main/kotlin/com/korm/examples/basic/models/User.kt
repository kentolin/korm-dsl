package com.korm.examples.basic.models

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int?
)