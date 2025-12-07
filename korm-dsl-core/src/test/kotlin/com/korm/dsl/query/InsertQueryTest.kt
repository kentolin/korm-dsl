// korm-dsl/korm-dsl-core/src/test/kotlin/com/korm/dsl/query/InsertQueryTest.kt

package com.korm.dsl.query

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class InsertQueryTest : StringSpec({

    object Users : Table("users") {
        val id = long("id").autoIncrement()
        val name = varchar("name", 100).notNull()
        val email = varchar("email", 255).notNull()
        override val primaryKey = PrimaryKey(id)
    }

    "should generate INSERT query" {
        val query = InsertQuery(Users).apply {
            set(Users.name, "John Doe")
            set(Users.email, "john@example.com")
        }

        query.toSql() shouldBe "INSERT INTO users (name, email) VALUES (?, ?)"
    }
})
