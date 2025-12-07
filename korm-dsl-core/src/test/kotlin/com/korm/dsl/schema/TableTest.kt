// korm-dsl/korm-dsl-core/src/test/kotlin/com/korm/dsl/schema/TableTest.kt

package com.korm.dsl.schema

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

class TableTest : StringSpec({

    object TestTable : Table("test_table") {
        val id = long("id").autoIncrement()
        val name = varchar("name", 100).notNull()
        val email = varchar("email", 255).unique()
        val age = int("age")
        val active = boolean("active").default(true)

        override val primaryKey = PrimaryKey(id)
    }

    "should generate CREATE TABLE statement" {
        val sql = TestTable.createStatement()

        sql shouldContain "CREATE TABLE IF NOT EXISTS test_table"
        sql shouldContain "id BIGINT AUTO_INCREMENT"
        sql shouldContain "name VARCHAR(100) NOT NULL"
        sql shouldContain "email VARCHAR(255) UNIQUE"
        sql shouldContain "PRIMARY KEY (id)"
    }

    "should get all columns" {
        val columns = TestTable.getColumns()

        columns.size shouldBe 5
        columns.map { it.name } shouldBe listOf("id", "name", "email", "age", "active")
    }
})
