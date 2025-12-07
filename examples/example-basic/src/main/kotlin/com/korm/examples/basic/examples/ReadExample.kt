// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/examples/ReadExample.kt

package com.korm.examples.basic.examples

import com.korm.examples.basic.repositories.UserRepository

class ReadExample(private val userRepository: UserRepository) {

    fun run() {
        println("=== Read Example ===")

        // Find all users
        val allUsers = userRepository.findAll()
        println("All users (${allUsers.size}):")
        allUsers.forEach { println("  - $it") }

        // Find by ID
        println("\nFinding user by ID (1):")
        val user = userRepository.findById(1)
        println("  Found: $user")

        // Find by email
        println("\nFinding user by email:")
        val userByEmail = userRepository.findByEmail("alice@example.com")
        println("  Found: $userByEmail")

        // Find by name (partial match)
        println("\nFinding users with 'Bob' in name:")
        val usersByName = userRepository.findByName("Bob")
        usersByName.forEach { println("  - $it") }

        // Find active users
        println("\nFinding active users:")
        val activeUsers = userRepository.findActive()
        println("  Found ${activeUsers.size} active users")
    }
}
