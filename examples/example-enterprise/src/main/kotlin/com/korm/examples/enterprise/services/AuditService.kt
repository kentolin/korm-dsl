// korm-dsl/examples/example-enterprise/src/main/kotlin/com/korm/examples/enterprise/services/AuditService.kt

package com.korm.examples.enterprise.services

import com.korm.dsl.core.Database
import com.korm.examples.enterprise.models.*
import java.sql.ResultSet
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class AuditService(private val database: Database) {

    fun logAction(
        userId: Long?,
        action: String,
        entityType: String,
        entityId: Long?,
        oldValue: String? = null,
        newValue: String? = null,
        ipAddress: String? = null,
        userAgent: String? = null
    ): AuditLog {
        val auditId = database.transaction {
            val query = insertInto(AuditLogs) {
                userId?.let { uid -> it[AuditLogs.userId] = uid }
                it[AuditLogs.action] = action
                it[AuditLogs.entityType] = entityType
                entityId?.let { eid -> it[AuditLogs.entityId] = eid }
                oldValue?.let { ov -> it[AuditLogs.oldValue] = ov }
                newValue?.let { nv -> it[AuditLogs.newValue] = nv }
                ipAddress?.let { ip -> it[AuditLogs.ipAddress] = ip }
                userAgent?.let { ua -> it[AuditLogs.userAgent] = ua }
            }
            insert(query)
        }

        return findById(auditId)!!
    }

    fun findById(id: Long): AuditLog? {
        return database.transaction {
            val query = from(AuditLogs).where(AuditLogs.id eq id)
            val results = select(query, ::mapAuditLog)
            results.firstOrNull()
        }
    }

    fun findByUser(userId: Long, limit: Int = 100): List<AuditLog> {
        return database.transaction {
            val query = from(AuditLogs)
                .where(AuditLogs.userId eq userId)
                .orderBy(AuditLogs.createdAt, "DESC")
                .limit(limit)
            select(query, ::mapAuditLog)
        }
    }

    fun findByEntity(entityType: String, entityId: Long): List<AuditLog> {
        return database.transaction {
            val query = from(AuditLogs)
                .where(
                    (AuditLogs.entityType eq entityType) and
                        (AuditLogs.entityId eq entityId)
                )
                .orderBy(AuditLogs.createdAt, "DESC")
            select(query, ::mapAuditLog)
        }
    }

    fun findByAction(action: String, limit: Int = 100): List<AuditLog> {
        return database.transaction {
            val query = from(AuditLogs)
                .where(AuditLogs.action eq action)
                .orderBy(AuditLogs.createdAt, "DESC")
                .limit(limit)
            select(query, ::mapAuditLog)
        }
    }

    fun findRecent(limit: Int = 100): List<AuditLog> {
        return database.transaction {
            val query = from(AuditLogs)
                .orderBy(AuditLogs.createdAt, "DESC")
                .limit(limit)
            select(query, ::mapAuditLog)
        }
    }

    private fun mapAuditLog(rs: ResultSet): AuditLog {
        return AuditLog(
            id = rs.getLong("id"),
            userId = rs.getLong("user_id").takeIf { it > 0 },
            action = rs.getString("action"),
            entityType = rs.getString("entity_type"),
            entityId = rs.getLong("entity_id").takeIf { it > 0 },
            oldValue = rs.getString("old_value"),
            newValue = rs.getString("new_value"),
            ipAddress = rs.getString("ip_address"),
            userAgent = rs.getString("user_agent"),
            createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(rs.getLong("created_at")),
                ZoneId.systemDefault()
            )
        )
    }
}
