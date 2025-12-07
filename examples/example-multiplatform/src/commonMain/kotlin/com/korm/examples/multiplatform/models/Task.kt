// examples/example-multiplatform/src/commonMain/kotlin/com/korm/examples/multiplatform/models/Task.kt

package com.korm.examples.multiplatform.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Task data model - shared across all platforms.
 */
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String?,
    val priority: TaskPriority,
    val status: TaskStatus,
    val dueDate: Instant?,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now()
)

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE,
    CANCELLED
}

/**
 * Tasks table definition using KORM-DSL.
 * This table definition is shared across all platforms.
 */
object Tasks : Table("tasks") {
    val id = long("id").autoIncrement()
    val title = varchar("title", 255).notNull()
    val description = text("description")
    val priority = varchar("priority", 20).default("MEDIUM").notNull()
    val status = varchar("status", 20).default("TODO").notNull()
    val dueDate = timestamp("due_date")
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()
    val updatedAt = timestamp("updated_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_tasks_status", listOf("status"))
        index("idx_tasks_priority", listOf("priority"))
        index("idx_tasks_due_date", listOf("due_date"))
    }
}
