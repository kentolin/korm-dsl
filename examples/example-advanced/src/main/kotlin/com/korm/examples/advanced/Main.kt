package com.korm.examples.advanced

import com.korm.dsl.core.ConnectionPool
import com.korm.dsl.core.Database
import com.korm.dsl.dialect.H2Dialect
import com.korm.dsl.query.*
import com.korm.dsl.schema.create
import com.korm.dsl.expressions.*
import com.korm.dsl.validation.*
import com.korm.examples.advanced.models.*

fun main() {
    println("=== KORM DSL Advanced Features Example ===\n")

    val pool = ConnectionPool.create(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        driver = "org.h2.Driver"
    )

    val db = Database(H2Dialect, pool)

    try {
        // Create tables
        println("1. Creating tables...")
        Users.create(db)
        Posts.create(db)
        Comments.create(db)
        println("✓ Tables created\n")

        // Feature 1: Validation
        demonstrateValidation()

        // Feature 2: Batch Operations
        demonstrateBatchOperations(db)

        // Feature 3: JOIN with Aggregates
        demonstrateJoinWithAggregates(db)

        println("✓ All advanced features demonstrated successfully!")

    } finally {
        db.close()
    }

    println("\n=== Example completed ===")
}

fun demonstrateValidation() {
    println("2. Validation Framework")
    println("-".repeat(70))

    // Example 1: Basic validation
    val usernameValidator = Validator<String>()
        .addRule(notNull("username"))
        .addRule(stringLength("username", min = 3, max = 20))

    val result1 = usernameValidator.validate("ab")
    println("  Validating 'ab': ${if (result1.isValid()) "✓ Valid" else "✗ ${(result1 as ValidationResult.Invalid).errors}"}")

    val result2 = usernameValidator.validate("alice")
    println("  Validating 'alice': ${if (result2.isValid()) "✓ Valid" else "✗ Invalid"}")

    // Example 2: Email validation
    val emailValidator = Validator<String>()
        .addRule(notNull("email"))
        .addRule(email("email"))

    val result3 = emailValidator.validate("invalid-email")
    println("  Validating 'invalid-email': ${if (result3.isValid()) "✓ Valid" else "✗ ${(result3 as ValidationResult.Invalid).errors}"}")

    val result4 = emailValidator.validate("alice@example.com")
    println("  Validating 'alice@example.com': ${if (result4.isValid()) "✓ Valid" else "✗ Invalid"}")

    // Example 3: Numeric range validation
    val ageValidator = Validator<Int>()
        .addRule(range("age", min = 18, max = 120))

    val result5 = ageValidator.validate(15)
    println("  Validating age 15: ${if (result5.isValid()) "✓ Valid" else "✗ ${(result5 as ValidationResult.Invalid).errors}"}")

    val result6 = ageValidator.validate(25)
    println("  Validating age 25: ${if (result6.isValid()) "✓ Valid" else "✗ Invalid"}")

    // Example 4: One-of validation
    val statusValidator = Validator<String>()
        .addRule(oneOf("status", "active", "inactive", "pending"))

    val result7 = statusValidator.validate("deleted")
    println("  Validating status 'deleted': ${if (result7.isValid()) "✓ Valid" else "✗ ${(result7 as ValidationResult.Invalid).errors}"}")

    val result8 = statusValidator.validate("active")
    println("  Validating status 'active': ${if (result8.isValid()) "✓ Valid" else "✗ Invalid"}")

    // Example 5: Custom validation
    val customValidator = Validator<String>()
        .addRule(custom<String>("password", "Password must contain uppercase") {
            it?.any { c -> c.isUpperCase() } ?: false
        })

    val result9 = customValidator.validate("lowercase")
    println("  Validating password 'lowercase': ${if (result9.isValid()) "✓ Valid" else "✗ ${(result9 as ValidationResult.Invalid).errors}"}")

    val result10 = customValidator.validate("Password123")
    println("  Validating password 'Password123': ${if (result10.isValid()) "✓ Valid" else "✗ Invalid"}")

    // Example 6: Validation context (multiple fields)
    val ctx = ValidationContext()
    ctx.validate("username", "ab", usernameValidator)
    ctx.validate("email", "invalid", emailValidator)
    ctx.validate("age", 15, ageValidator)

    println("\n  Multi-field validation:")
    if (ctx.isValid()) {
        println("    ✓ All fields valid")
    } else {
        println("    ✗ Validation errors:")
        ctx.getErrorMessages().forEach { println("      - $it") }
    }
    println()
}

fun demonstrateBatchOperations(db: Database) {
    println("3. Batch Operations")
    println("-".repeat(70))

    // Example 1: Batch Insert
    println("  Batch inserting 1000 users...")
    val startTime = System.currentTimeMillis()

    val batchInsert = Users.batchInsert(db)
    repeat(1000) { i ->
        batchInsert.addBatch {
            set(Users.username, "user$i")
            set(Users.email, "user$i@example.com")
            set(Users.age, 20 + (i % 50))
            set(Users.status, if (i % 2 == 0) "active" else "inactive")
        }
    }

    val inserted = batchInsert.execute(batchSize = 100)
    val duration = System.currentTimeMillis() - startTime
    println("  ✓ Inserted $inserted users in ${duration}ms")

    // Example 2: Batch Update
    println("\n  Batch updating user statuses...")
    val batchUpdate = Users.batchUpdate(db)

    for (i in 1..100) {
        batchUpdate.addBatch(
            updates = mapOf(Users.status to "premium"),
            whereColumn = Users.id,
            whereValue = i
        )
    }

    val updated = batchUpdate.execute()
    println("  ✓ Updated $updated users to premium status")

    // Verify
    val premiumCount = Users.select(db)
        .where(Users.status, "premium")
        .execute { rs -> rs.getInt("id") }
        .size

    println("  ✓ Verified: $premiumCount premium users\n")
}

fun demonstrateJoinWithAggregates(db: Database) {
    println("4. JOIN with Aggregates")
    println("-".repeat(70))

    // Insert test data
    println("  Setting up test data...")

    // Insert posts for first 10 users
    val postInsert = Posts.batchInsert(db)
    for (userId in 1..10) {
        repeat(5) { postNum ->
            postInsert.addBatch {
                set(Posts.userId, userId)
                set(Posts.title, "Post $postNum by User $userId")
                set(Posts.content, "Content of post $postNum")
                set(Posts.likes, (0..100).random())
                set(Posts.createdAt, "2024-01-${(postNum + 1).toString().padStart(2, '0')}")
            }
        }
    }
    postInsert.execute()

    // Insert comments
    val commentInsert = Comments.batchInsert(db)
    for (postId in 1..50) {
        repeat(3) {
            commentInsert.addBatch {
                set(Comments.postId, postId)
                set(Comments.userId, (1..10).random())
                set(Comments.content, "Great post!")
                set(Comments.createdAt, "2024-01-15")
            }
        }
    }
    commentInsert.execute()
    println("  ✓ Test data created\n")

    // Example 1: User post statistics with JOIN and aggregates
    println("  User post statistics (JOIN + GROUP BY + aggregates):")
    val userStats = Posts.select(db)
        .selectWithAggregate(
            Users.username,
            aggregates = listOf(
                count(alias = "post_count"),
                sum(Posts.likes, alias = "total_likes"),
                avg(Posts.likes, alias = "avg_likes")
            )
        )
        .innerJoinOn(Users, Posts.userId, Users.id)
        .groupBy(Users.username, Users.id)
        .execute { rs ->
            mapOf(
                "username" to rs.getString("username"),
                "posts" to rs.getInt("post_count"),
                "total_likes" to rs.getInt("total_likes"),
                "avg_likes" to rs.getDouble("avg_likes")
            )
        }

    userStats.take(5).forEach { stat ->
        println("    ${stat["username"]}: ${stat["posts"]} posts, ${stat["total_likes"]} likes (avg: ${"%.1f".format(stat["avg_likes"])})")
    }

    // Example 2: Popular users (HAVING with aggregates and JOIN)
    println("\n  Popular users (posts with avg >50 likes):")
    val popularUsers = Posts.select(db)
        .selectWithAggregate(
            Users.username,
            aggregates = listOf(
                count(alias = "post_count"),
                avg(Posts.likes, alias = "avg_likes")
            )
        )
        .innerJoinOn(Users, Posts.userId, Users.id)
        .groupBy(Users.username, Users.id)
        .having(avg(Posts.likes), ">", 50.0)
        .execute { rs ->
            mapOf(
                "username" to rs.getString("username"),
                "posts" to rs.getInt("post_count"),
                "avg_likes" to rs.getDouble("avg_likes")
            )
        }

    popularUsers.forEach { user ->
        println("    ${user["username"]}: ${user["posts"]} posts, avg ${"%.1f".format(user["avg_likes"])} likes")
    }

    // Example 3: Comment statistics per post
    println("\n  Comment statistics per post:")
    val commentStats = Comments.select(db)
        .selectWithAggregate(
            Posts.title,
            aggregates = listOf(
                count(alias = "comment_count")
            )
        )
        .innerJoinOn(Posts, Comments.postId, Posts.id)
        .groupBy(Posts.title, Posts.id)
        .execute { rs ->
            Pair(rs.getString("title"), rs.getInt("comment_count"))
        }

    commentStats.take(5).forEach { (title, count) ->
        println("    \"$title\": $count comments")
    }

    println()
}