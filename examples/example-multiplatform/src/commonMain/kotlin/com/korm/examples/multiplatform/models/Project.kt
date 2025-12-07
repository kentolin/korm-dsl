// examples/example-multiplatform/src/commonMain/kotlin/com/korm/examples/multiplatform/models/Project.kt

package com.korm.examples.multiplatform.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Project data model - shared across all platforms.
 */
data class Project(
    val id: Long = 0,
    val name: String,
    val description: String?,
    val color: String,
    val isArchived: Boolean = false,
    val createdAt: Instant = Clock.System.now()
)

/**
 * Projects table definition using KORM-DSL.
 */
object Projects : Table("projects") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100).notNull()
    val description = text("description")
    val color = varchar("color", 7).default("#2196F3").notNull()
    val isArchived = boolean("is_archived").default(false).notNull()
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_projects_name", listOf("name"))
        index("idx_projects_archived", listOf("is_archived"))
    }
}
