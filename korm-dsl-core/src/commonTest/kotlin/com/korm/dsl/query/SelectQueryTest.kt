// korm-dsl/korm-dsl-core/src/test/kotlin/com/korm/dsl/query/SelectQueryTest.kt

package com.korm.dsl.query

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SelectQueryTest : StringSpec({

    object Users : Table("users") {
        val id = long("id").autoIncrement()
        val name = varchar("name", 100).notNull()
        val email = varchar("email", 255).notNull()
        override val primaryKey = PrimaryKey(id)
    }

    "should generate simple SELECT query" {
        val query = SelectQuery(Users)
        query.toSql() shouldBe "SELECT * FROM users"
    }

    "should generate SELECT with WHERE clause" {
        val query = SelectQuery(Users)
            .where(Users.id eq 1L)

        query.toSql() shouldBe "SELECT * FROM users WHERE id = 1"
    }

    "should generate SELECT with multiple conditions" {
        val query = SelectQuery(Users)
            .where(Users.id eq 1L)
            .and(Users.name eq "John")

        query.toSql() shouldBe "SELECT * FROM users WHERE id = 1 AND name = 'John'"
    }

    "should generate SELECT with ORDER BY" {
        val query = SelectQuery(Users)
            .orderBy(Users.name, "ASC")

        query.toSql() shouldBe "SELECT * FROM users ORDER BY name ASC"
    }

    "should generate SELECT with LIMIT" {
        val query = SelectQuery(Users)
            .limit(10)

        query.toSql() shouldBe "SELECT * FROM users LIMIT 10"
    }

    "should generate SELECT with LIMIT and OFFSET" {
        val query = SelectQuery(Users)
            .limit(10)
            .offset(20)

        query.toSql() shouldBe "SELECT * FROM users LIMIT 10 OFFSET 20"
    }
})
