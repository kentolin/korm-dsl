// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/examples/CreateExample.kt

package com.korm.examples.basic.examples

import com.korm.examples.basic.models.CreateUserDTO
import com.korm.examples.basic.repositories.UserRepository

class CreateExample(private val userRepository: UserRepository) {

    fun run() {
        println("=== Create Example ===")

        // Create users
        val user1 = userRepository.create(
            CreateUserDTO(
                name = "Alice Johnson",
                email = "alice@example.com",
                age = 28
            )
        )
        println("Created user: $user1")

        val user2 = userRepository.create(
            CreateUserDTO(
                name = "Bob Smith",
                email = "bob@example.com",
                age = 35
            )
        )
        println("Created user: $user2")

        val user3 = userRepository.create(
            CreateUserDTO(
                name = "Charlie Brown",
                email = "charlie@example.com"
            )
        )
        println("Created user: $user3")

        println("Successfully created ${listOf(user1, user2, user3).size} users")
    }
}
