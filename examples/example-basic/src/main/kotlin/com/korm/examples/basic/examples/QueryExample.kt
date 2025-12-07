// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/examples/QueryExample.kt

package com.korm.examples.basic.examples

import com.korm.examples.basic.repositories.UserRepository

class QueryExample(private val userRepository: UserRepository) {

    fun run() {
        println("=== Query Example ===")

        // Find active users
        println("Active users:")
        val activeUsers = userRepository.findActive()
        activeUsers.forEach { println("  - ${it.name} (${it.email})") }

        // Find users by partial name match
        println("\nUsers with 'Smith' in name:")
        val smithUsers = userRepository.findByName("Smith")
        smithUsers.forEach { println("  - ${it.name}") }

        // Find specific email
        println("\nLooking for bob@example.com:")
        val bob = userRepository.findByEmail("bob@example.com")
        println("  Found: ${bob?.name ?: "Not found"}")
    }
}
