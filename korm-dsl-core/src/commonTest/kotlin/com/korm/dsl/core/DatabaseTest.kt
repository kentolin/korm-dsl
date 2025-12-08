// korm-dsl/korm-dsl-core/src/test/kotlin/com/korm/dsl/core/DatabaseTest.kt

package com.korm.dsl.core

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class DatabaseTest : FunSpec({

    val postgres = PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
        .withDatabaseName("test")
        .withUsername("test")
        .withPassword("test")

    beforeSpec {
        postgres.start()
    }

    afterSpec {
        postgres.stop()
    }

    test("should connect to database") {
        val database = Database.connect(
            url = postgres.jdbcUrl,
            driver = postgres.driverClassName,
            user = postgres.username,
            password = postgres.password
        )

        database shouldNotBe null
        database.close()
    }

    test("should create tables") {
        val database = Database.connect(
            url = postgres.jdbcUrl,
            driver = postgres.driverClassName,
            user = postgres.username,
            password = postgres.password
        )

        object TestTable : Table("test_table") {
            val id = long("id").autoIncrement()
            val name = varchar("name", 100).notNull()
            override val primaryKey = PrimaryKey(id)
        }

        database.createTables(TestTable)

        // Verify table exists
        database.transaction {
            val sql = "SELECT COUNT(*) FROM test_table"
            execute(sql) shouldBe true
        }

        database.close()
    }

    test("should execute transaction") {
        val database = Database.connect(
            url = postgres.jdbcUrl,
            driver = postgres.driverClassName,
            user = postgres.username,
            password = postgres.password
        )

        val result = database.transaction {
            execute("SELECT 1")
        }

        result shouldBe true
        database.close()
    }
})
