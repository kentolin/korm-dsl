// korm-dsl/examples/example-basic/src/main/kotlin/com/korm/examples/basic/models/Tables.kt

package com.korm.examples.basic.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).unique().notNull()
    val age = int("age")
    val active = boolean("active").default(true).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)
}
