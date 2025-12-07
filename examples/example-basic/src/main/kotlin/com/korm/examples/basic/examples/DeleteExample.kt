// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/examples/DeleteExample.kt

package com.korm.examples.basic.examples

import com.korm.examples.basic.repositories.UserRepository

class DeleteExample(private val userRepository: UserRepository) {

    fun run() {
        println("=== Delete Example ===")

        // Show all users before deletion
        val beforeCount = userRepository.findAll().size
        println("Users before deletion: $beforeCount")

        // Delete a user
        val deleted = userRepository.delete(3)
        println("Deleted user with ID 3: $deleted")

        // Show remaining users
        val afterCount = userRepository.findAll().size
        println("Users after deletion: $afterCount")

        // Verify deletion
        val user = userRepository.findById(3)
        println("User 3 still exists: ${user != null}")
    }
}
