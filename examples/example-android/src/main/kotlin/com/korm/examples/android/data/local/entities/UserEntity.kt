// examples/example-android/src/main/kotlin/com/korm/examples/android/data/local/entities/UserEntity.kt

package com.korm.examples.android.data.local.entities

data class UserEntity(
    val id: Long = 0,
    val name: String,
    val email: String,
    val age: Int,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
