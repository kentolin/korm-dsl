// korm-dsl/examples/example-relationships/src/main/kotlin/com/korm/examples/relationships/models/Category.kt

package com.korm.examples.relationships.models

data class Category(
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
