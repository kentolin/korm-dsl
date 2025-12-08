// korm-dsl/korm-dsl-cache/src/main/kotlin/com/korm/dsl/cache/providers/RedisCache.kt

package com.korm.dsl.cache.providers

import com.korm.dsl.cache.Cache
import com.korm.dsl.cache.CacheStats
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

/**
 * Redis-based cache implementation.
 */
class RedisCache<K : Any, V : Any>(
    private val jedisPool: JedisPool,
    private val keyPrefix: String = "korm:cache:",
    private val serializer: CacheSerializer<V>
) : Cache<K, V> {

    private val hitCount = AtomicLong(0)
    private val missCount = AtomicLong(0)

    constructor(
        host: String = "localhost",
        port: Int = 6379,
        password: String? = null,
        database: Int = 0,
        keyPrefix: String = "korm:cache:",
        serializer: CacheSerializer<V>
    ) : this(
        createJedisPool(host, port, password, database),
        keyPrefix,
        serializer
    )

    companion object {
        private fun createJedisPool(
            host: String,
            port: Int,
            password: String?,
            database: Int
        ): JedisPool {
            val config = JedisPoolConfig().apply {
                maxTotal = 128
                maxIdle = 32
                minIdle = 8
                testOnBorrow = true
                testOnReturn = true
                testWhileIdle = true
            }

            return if (password != null) {
                JedisPool(config, host, port, 2000, password, database)
            } else {
                JedisPool(config, host, port, 2000, null, database)
            }
        }
    }

    override fun get(key: K): V? {
        return jedisPool.resource.use { jedis ->
            val redisKey = buildKey(key)
            val value = jedis.get(redisKey)

            if (value != null) {
                hitCount.incrementAndGet()
                serializer.deserialize(value)
            } else {
                missCount.incrementAndGet()
                null
            }
        }
    }

    override fun put(key: K, value: V) {
        jedisPool.resource.use { jedis ->
            val redisKey = buildKey(key)
            val serialized = serializer.serialize(value)
            jedis.set(redisKey, serialized)
        }
    }

    override fun put(key: K, value: V, ttl: Duration) {
        jedisPool.resource.use { jedis ->
            val redisKey = buildKey(key)
            val serialized = serializer.serialize(value)
            jedis.setex(redisKey, ttl.seconds, serialized)
        }
    }

    override fun remove(key: K) {
        jedisPool.resource.use { jedis ->
            val redisKey = buildKey(key)
            jedis.del(redisKey)
        }
    }

    override fun containsKey(key: K): Boolean {
        return jedisPool.resource.use { jedis ->
            val redisKey = buildKey(key)
            jedis.exists(redisKey)
        }
    }

    override fun clear() {
        jedisPool.resource.use { jedis ->
            val pattern = "$keyPrefix*"
            val keys = jedis.keys(pattern)
            if (keys.isNotEmpty()) {
                jedis.del(*keys.toTypedArray())
            }
        }
    }

    override fun keys(): Set<K> {
        return jedisPool.resource.use { jedis ->
            val pattern = "$keyPrefix*"
            jedis.keys(pattern).map { it.removePrefix(keyPrefix) as K }.toSet()
        }
    }

    override fun size(): Long {
        return jedisPool.resource.use { jedis ->
            val pattern = "$keyPrefix*"
            jedis.keys(pattern).size.toLong()
        }
    }

    override fun stats(): CacheStats {
        return CacheStats(
            hitCount = hitCount.get(),
            missCount = missCount.get(),
            size = size()
        )
    }

    private fun buildKey(key: K): String {
        return "$keyPrefix$key"
    }

    /**
     * Close the connection pool.
     */
    fun close() {
        jedisPool.close()
    }
}

/**
 * Cache serializer interface.
 */
interface CacheSerializer<T> {
    fun serialize(value: T): String
    fun deserialize(data: String): T
}

/**
 * JSON serializer for cache values.
 */
class JsonCacheSerializer<T>(
    private val clazz: Class<T>
) : CacheSerializer<T> {
    private val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()

    override fun serialize(value: T): String {
        return objectMapper.writeValueAsString(value)
    }

    override fun deserialize(data: String): T {
        return objectMapper.readValue(data, clazz)
    }
}
