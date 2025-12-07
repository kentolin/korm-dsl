// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/examples/UpdateExample.kt

package com.korm.examples.basic.examples

import com.korm.examples.basic.models.UpdateUserDTO
import com.korm.examples.basic.repositories.UserRepository

class UpdateExample(private val userRepository: UserRepository) {

    fun run() {
        println("=== Update Example ===")

        // Get a user
        val user = userRepository.findById(1)
        println("Original user: $user")

        // Update the user
        val updated = userRepository.update(
            1,
            UpdateUserDTO(
                name = "Alice Smith",
                age = 29
            )
        )
        println("Updated user: $updated")

        // Update another field
        val updated2 = userRepository.update(
            1,
            UpdateUserDTO(active = false)
        )
        println("Deactivated user: $updated2")
    }
}
