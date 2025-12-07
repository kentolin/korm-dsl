// examples/example-android/src/main/kotlin/com/korm/examples/android/data/local/entities/Tables.kt

package com.korm.examples.android.data.local.entities

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table

object Users : Table("users") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).notNull()
    val email = varchar("email", 255).unique().notNull()
    val age = int("age").notNull()
    val isActive = boolean("is_active").default(true).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_users_email", listOf("email"), unique = true)
        index("idx_users_name", listOf("name"))
    }
}
