// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/models/AuditLog.kt

package com.korm.examples.enterprise.models

import com.korm.dsl.schema.PrimaryKey
import com.korm.dsl.schema.Table
import java.time.LocalDateTime

data class AuditLog(
    val id: Long = 0,
    val userId: Long?,
    val action: String,
    val entityType: String,
    val entityId: Long?,
    val oldValue: String? = null,
    val newValue: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

object AuditLogs : Table("audit_logs") {
    val id = long("id").autoIncrement()
    val userId = long("user_id")
    val action = varchar("action", 50).notNull()
    val entityType = varchar("entity_type", 50).notNull()
    val entityId = long("entity_id")
    val oldValue = text("old_value")
    val newValue = text("new_value")
    val ipAddress = varchar("ip_address", 45)
    val userAgent = varchar("user_agent", 255)
    val createdAt = timestamp("created_at").default(System.currentTimeMillis()).notNull()

    override val primaryKey = PrimaryKey(id)

    init {
        index("idx_audit_logs_user_id", listOf("user_id"))
        index("idx_audit_logs_entity", listOf("entity_type", "entity_id"))
        index("idx_audit_logs_created_at", listOf("created_at"))
    }
}
