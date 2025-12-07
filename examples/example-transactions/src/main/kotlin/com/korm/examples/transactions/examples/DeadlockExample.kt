// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/DeadlockExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal
import kotlin.concurrent.thread

class DeadlockExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Deadlock Prevention Example ===")
        println("Demonstrating deadlock prevention through lock ordering\n")

        // Create accounts
        val user1 = userService.createUser("henry", "henry@example.com")
        val user2 = userService.createUser("iris", "iris@example.com")

        val account1 = accountService.createAccount(user1.id, "ACC008", BigDecimal("1000.00"))
        val account2 = accountService.createAccount(user2.id, "ACC009", BigDecimal("1000.00"))

        println("Account 1 (ID: ${account1.id}): ${account1.balance}")
        println("Account 2 (ID: ${account2.id}): ${account2.balance}\n")

        println("Starting concurrent transfers in opposite directions...")
        println("  Thread 1: ACC008 -> ACC009 ($100)")
        println("  Thread 2: ACC009 -> ACC008 ($50)\n")

        val results = mutableListOf<String>()

        val thread1 = thread {
            try {
                accountService.transfer(account1.id, account2.id, BigDecimal("100.00"))
                results.add("Thread 1: Transfer completed successfully")
            } catch (e: Exception) {
                results.add("Thread 1: ${e.message}")
            }
        }

        // Small delay to ensure both threads start around same time
        Thread.sleep(50)

        val thread2 = thread {
            try {
                accountService.transfer(account2.id, account1.id, BigDecimal("50.00"))
                results.add("Thread 2: Transfer completed successfully")
            } catch (e: Exception) {
                results.add("Thread 2: ${e.message}")
            }
        }

        thread1.join()
        thread2.join()

        println("Results:")
        results.forEach { println("  $it") }

        val balance1Final = accountService.getBalance(account1.id)
        val balance2Final = accountService.getBalance(account2.id)

        println("\n--- Final Balances ---")
        println("Account 1: $balance1Final")
        println("Account 2: $balance2Final")
        println("\nDeadlock was prevented by locking accounts in consistent order!")
        println("(AccountService locks accounts by ID in ascending order)")
    }
}
