// korm-dsl/korm-dsl-migrations/src/test/kotlin/com/korm/dsl/migrations/MigrationEngineTest.kt

package com.korm.dsl.migrations

import com.korm.dsl.core.Database
import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

class MigrationEngineTest : FunSpec({

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

    test("should execute migrations in order") {
        val database = Database.connect(
            url = postgres.jdbcUrl,
            driver = postgres.driverClassName,
            user = postgres.username,
            password = postgres.password
        )

        object Users : Table("users") {
            val id = long("id").autoIncrement()
            val name = varchar("name", 100).notNull()
            override val primaryKey = PrimaryKey(id)
        }

        val migration1 = migration(1, "Create users table") {
            up {
                createTable(Users)
            }
            down {
                dropTable("users")
            }
        }

        val migration2 = migration(2, "Add email column") {
            up {
                addColumn("users", "email VARCHAR(255)")
            }
            down {
                dropColumn("users", "email")
            }
        }

        val engine = MigrationEngine(database)
        val results = engine.migrate(listOf(migration1, migration2))

        results.size shouldBe 2
        results.all { it is MigrationResult.Success } shouldBe true

        database.close()
    }
})
