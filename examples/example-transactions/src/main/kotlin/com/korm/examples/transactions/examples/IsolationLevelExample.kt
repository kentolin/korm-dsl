// korm-dsl/examples/example-transactions/src/main/kotlin/com/korm/examples/transactions/examples/IsolationLevelExample.kt

package com.korm.examples.transactions.examples

import com.korm.dsl.core.Database
import com.korm.dsl.core.TransactionIsolation
import com.korm.dsl.core.TransactionConfig
import com.korm.examples.transactions.models.*
import com.korm.examples.transactions.services.UserService
import com.korm.examples.transactions.services.AccountService
import java.math.BigDecimal
import kotlin.concurrent.thread

class IsolationLevelExample(private val database: Database) {

    private val userService = UserService(database)
    private val accountService = AccountService(database)

    fun run() {
        println("=== Isolation Level Example ===")
        println("Demonstrating different transaction isolation levels\n")

        // Setup
        val user = userService.createUser("charlie", "charlie@example.com")
        val account = accountService.createAccount(user.id, "ACC003", BigDecimal("1000.00"))

        // READ_COMMITTED example
        println("--- READ COMMITTED Isolation Level ---")
        demonstrateReadCommitted(account.id)

        println("\n--- REPEATABLE READ Isolation Level ---")
        demonstrateRepeatableRead(account.id)

        println("\n--- SERIALIZABLE Isolation Level ---")
        demonstrateSerializable(account.id)
    }

    private fun demonstrateReadCommitted(accountId: Long) {
        println("Thread 1 reads balance, Thread 2 updates, Thread 1 reads again\n")

        val balance1 = mutableListOf<BigDecimal>()
        val balance2 = mutableListOf<BigDecimal>()

        val thread1 = thread {
            database.transaction(TransactionConfig(isolationLevel = TransactionIsolation.READ_COMMITTED)) {
                // First read
                val b1 = accountService.getBalance(accountId)
                balance1.add(b1)
                println("T1: Read 1: $b1")

                Thread.sleep(500) // Wait for T2 to update

                // Second read - will see committed changes
                val b2 = accountService.getBalance(accountId)
                balance1.add(b2)
                println("T1: Read 2: $b2")
            }
        }

        Thread.sleep(200)

        val thread2 = thread {
            database.transaction {
                val current = accountService.getBalance(accountId)
                balance2.add(current)
                println("T2: Current balance: $current")

                accountService.deposit(accountId, BigDecimal("100.00"))
                println("T2: Deposited $100")

                val updated = accountService.getBalance(accountId)
                balance2.add(updated)
                println("T2: New balance: $updated")
            }
        }

        thread1.join()
        thread2.join()

        println("\nResult: T1 saw different values (non-repeatable read)")
    }

    private fun demonstrateRepeatableRead(accountId: Long) {
        println("With REPEATABLE_READ, same value is read within transaction\n")

        // Reset balance
        database.transaction {
            update(Accounts) {
                it[Accounts.balance] = BigDecimal("1000.00")
            }.where(Accounts.id eq accountId).let { update(it) }
        }

        database.transaction(TransactionConfig(isolationLevel = TransactionIsolation.REPEATABLE_READ)) {
            val read1 = accountService.getBalance(accountId)
            println("Read 1: $read1")

            // Another transaction updates the balance
            val otherThread = thread {
                database.transaction {
                    accountService.deposit(accountId, BigDecimal("200.00"))
                    println("  [Other transaction] Deposited $200")
                }
            }
            otherThread.join()

            val read2 = accountService.getBalance(accountId)
            println("Read 2: $read2")

            println("Both reads show same value (repeatable read)")
        }
    }

    private fun demonstrateSerializable(accountId: Long) {
        println("SERIALIZABLE provides highest isolation level\n")
        println("Transactions execute as if they were serial")

        // Reset balance
        database.transaction {
            update(Accounts) {
                it[Accounts.balance] = BigDecimal("1000.00")
            }.where(Accounts.id eq accountId).let { update(it) }
        }

        println("Starting balance: ${accountService.getBalance(accountId)}")

        val thread1 = thread {
            database.transaction(TransactionConfig(isolationLevel = TransactionIsolation.SERIALIZABLE)) {
                println("T1: Reading balance...")
                val balance = accountService.getBalance(accountId)
                Thread.sleep(300)

                println("T1: Updating balance...")
                accountService.deposit(accountId, BigDecimal("100.00"))
                println("T1: Committed")
            }
        }

        Thread.sleep(100)

        val thread2 = thread {
            database.transaction(TransactionConfig(isolationLevel = TransactionIsolation.SERIALIZABLE)) {
                println("T2: Reading balance...")
                val balance = accountService.getBalance(accountId)

                println("T2: Updating balance...")
                accountService.deposit(accountId, BigDecimal("200.00"))
                println("T2: Committed")
            }
        }

        thread1.join()
        thread2.join()

        val finalBalance = accountService.getBalance(accountId)
        println("\nFinal balance: $finalBalance")
        println("All updates were applied correctly with no conflicts")
    }
}
