// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/models/User.kt

package com.korm.examples.basic.models

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    val age: Int? = null,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
