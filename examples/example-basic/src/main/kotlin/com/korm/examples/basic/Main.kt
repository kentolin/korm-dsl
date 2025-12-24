package com.korm.examples.basic

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.query.*
import com.korm.dsl.schema.create
import com.korm.dsl.schema.drop
import com.korm.examples.basic.models.User
import com.korm.examples.basic.models.Users

fun main() {
    println("=== KORM DSL Basic Example ===\n")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    val db = Database(H2Dialect, pool)

    try {
        println("1. Creating table...")
        Users.create(db)
        println("✓ Table created\n")

        println("2. Inserting users...")
        Users.insert(db)
            .set(Users.name, "Alice")
            .set(Users.email, "alice@example.com")
            .set(Users.age, 30)
            .execute()
        println("✓ Inserted Alice")

        Users.insert(db)
            .set(Users.name, "Bob")
            .set(Users.email, "bob@example.com")
            .set(Users.age, 25)
            .execute()
        println("✓ Inserted Bob\n")

        println("3. Selecting all users...")
        val users = Users.select(db).execute { rs ->
            User(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                age = rs.getObject("age") as? Int
            )
        }

        users.forEach { println("   ${it.id}: ${it.name} (${it.email}) - Age: ${it.age}") }
        println()

        println("4. Updating Bob's age...")
        Users.update(db)
            .set(Users.age, 26)
            .where(Users.name, "Bob")
            .execute()
        println("✓ Updated\n")

        println("5. Deleting Alice...")
        Users.delete(db)
            .where(Users.name, "Alice")
            .execute()
        println("✓ Deleted\n")

        println("6. Final user list...")
        val finalUsers = Users.select(db).execute { rs ->
            User(
                id = rs.getInt("id"),
                name = rs.getString("name"),
                email = rs.getString("email"),
                age = rs.getObject("age") as? Int
            )
        }
        println("   Total users: ${finalUsers.size}")
        finalUsers.forEach { println("   - ${it.name}") }

    } finally {
        db.close()
    }

    println("\n=== Example completed ===")
}