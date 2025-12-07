// korm-dsl/korm-dsl-cache/src/test/kotlin/com/korm/dsl/cache/InMemoryCacheTest.kt

package com.korm.dsl.cache

import com.korm.dsl.cache.providers.InMemoryCache
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.Duration

class InMemoryCacheTest : FunSpec({

    test("should store and retrieve values") {
        val cache = InMemoryCache<String, String>()

        cache.put("key1", "value1")
        cache.get("key1") shouldBe "value1"
    }

    test("should return null for missing keys") {
        val cache = InMemoryCache<String, String>()

        cache.get("missing") shouldBe null
    }

    test("should evict entries based on TTL") {
        val cache = InMemoryCache<String, String>()

        cache.put("key1", "value1", Duration.ofMillis(100))
        cache.get("key1") shouldNotBe null

        Thread.sleep(150)
        cache.get("key1") shouldBe null
    }

    test("should evict LRU entries when capacity is exceeded") {
        val config = CacheConfig(maxSize = 2, evictionStrategy = EvictionStrategyType.LRU)
        val cache = InMemoryCache<String, String>(config)

        cache.put("key1", "value1")
        cache.put("key2", "value2")
        cache.put("key3", "value3") // Should evict key1

        cache.get("key1") shouldBe null
        cache.get("key2") shouldNotBe null
        cache.get("key3") shouldNotBe null
    }

    test("should track hit and miss counts") {
        val cache = InMemoryCache<String, String>()

        cache.put("key1", "value1")
        cache.get("key1") // hit
        cache.get("key2") // miss

        val stats = cache.stats()
        stats.hitCount shouldBe 1
        stats.missCount shouldBe 1
    }
})
