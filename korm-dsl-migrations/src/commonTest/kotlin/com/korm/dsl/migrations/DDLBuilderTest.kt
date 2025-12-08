// korm-dsl/korm-dsl-migrations/src/test/kotlin/com/korm/dsl/migrations/DDLBuilderTest.kt

package com.korm.dsl.migrations

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class DDLBuilderTest : StringSpec({

    "should build CREATE TABLE statement" {
        val statements = ddl {
            execute("CREATE TABLE users (id BIGINT PRIMARY KEY, name VARCHAR(100))")
        }

        statements.size shouldBe 1
        statements[0] shouldContain "CREATE TABLE users"
    }

    "should build multiple DDL statements" {
        val statements = ddl {
            execute("CREATE TABLE users (id BIGINT PRIMARY KEY)")
            addColumn("users", "name VARCHAR(100)")
            createIndex("idx_users_name", "users", listOf("name"))
        }

        statements.size shouldBe 3
    }

    "should build ALTER TABLE statements" {
        val builder = DDLBuilder()
            .addColumn("users", "email VARCHAR(255)")
            .renameColumn("users", "name", "full_name")
            .dropColumn("users", "age")

        val statements = builder.build()

        statements.size shouldBe 3
        statements[0] shouldContain "ALTER TABLE users ADD COLUMN email"
        statements[1] shouldContain "RENAME COLUMN name TO full_name"
        statements[2] shouldContain "DROP COLUMN age"
    }
})
