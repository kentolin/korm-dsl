// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/models/UserDTO.kt

package com.korm.examples.basic.models

/**
 * Data Transfer Object for User creation.
 */
data class CreateUserDTO(
    val name: String,
    val email: String,
    val age: Int? = null
)

/**
 * Data Transfer Object for User updates.
 */
data class UpdateUserDTO(
    val name: String? = null,
    val email: String? = null,
    val age: Int? = null,
    val active: Boolean? = null
)
