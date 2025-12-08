// korm-dsl/korm-dsl-cache/src/test/kotlin/com/korm/dsl/cache/QueryCacheTest.kt

package com.korm.dsl.cache

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.Duration

class QueryCacheTest : StringSpec({

    "should cache query results" {
        val cache = QueryCache<String>()

        val result = listOf("value1", "value2")
        cache.put("SELECT * FROM users", emptyMap(), result)

        cache.get("SELECT * FROM users") shouldBe result
    }

    "should generate consistent keys for same query and params" {
        val cache = QueryCache<String>()

        val params = mapOf("id" to 1, "name" to "John")
        cache.put("SELECT * FROM users WHERE id = ? AND name = ?", params, listOf("user1"))

        val cached = cache.get("SELECT * FROM users WHERE id = ? AND name = ?", params)
        cached shouldBe listOf("user1")
    }

    "should invalidate specific queries" {
        val cache = QueryCache<String>()

        cache.put("SELECT * FROM users", emptyMap(), listOf("user1"))
        cache.invalidate("SELECT * FROM users")

        cache.get("SELECT * FROM users") shouldBe null
    }
})
