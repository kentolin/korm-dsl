// examples/example-multiplatform/src/commonMain/kotlin/com/korm/examples/multiplatform/repositories/ProjectRepository.kt

package com.korm.examples.multiplatform.repositories

import com.korm.dsl.core.Database
import com.korm.examples.multiplatform.models.Project
import com.korm.examples.multiplatform.models.Projects
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.sql.ResultSet

/**
 * Project repository - shared business logic across all platforms.
 */
class ProjectRepository(private val database: Database) {

    suspend fun createProject(
        name: String,
        description: String?,
        color: String = "#2196F3"
    ): Project = withContext(Dispatchers.Default) {
        val projectId = database.transaction {
            insertInto(Projects) {
                it[Projects.name] = name
                description?.let { desc -> it[Projects.description] = desc }
                it[Projects.color] = color
            }.let { insert(it) }
        }

        findById(projectId)!!
    }

    suspend fun findById(id: Long): Project? = withContext(Dispatchers.Default) {
        database.transaction {
            val query = from(Projects).where(Projects.id eq id)
            val results = select(query, ::mapProject)
            results.firstOrNull()
        }
    }

    suspend fun findAll(includeArchived: Boolean = false): List<Project> = withContext(Dispatchers.Default) {
        database.transaction {
            val query = if (includeArchived) {
                from(Projects).orderBy(Projects.name)
            } else {
                from(Projects)
                    .where(Projects.isArchived eq false)
                    .orderBy(Projects.name)
            }
            select(query, ::mapProject)
        }
    }

    suspend fun updateProject(
        id: Long,
        name: String? = null,
        description: String? = null,
        color: String? = null
    ): Project = withContext(Dispatchers.Default) {
        database.transaction {
            val query = update(Projects) {
                name?.let { n -> it[Projects.name] = n }
                description?.let { d -> it[Projects.description] = d }
                color?.let { c -> it[Projects.color] = c }
            }.where(Projects.id eq id)

            update(query)
        }

        findById(id)!!
    }

    suspend fun archiveProject(id: Long): Project = withContext(Dispatchers.Default) {
        database.transaction {
            val query = update(Projects) {
                it[Projects.isArchived] = true
            }.where(Projects.id eq id)

            update(query)
        }

        findById(id)!!
    }

    suspend fun unarchiveProject(id: Long): Project = withContext(Dispatchers.Default) {
        database.transaction {
            val query = update(Projects) {
                it[Projects.isArchived] = false
            }.where(Projects.id eq id)

            update(query)
        }

        findById(id)!!
    }

    suspend fun deleteProject(id: Long): Boolean = withContext(Dispatchers.Default) {
        val deleted = database.transaction {
            val query = deleteFrom(Projects) {
                where(Projects.id eq id)
            }
            delete(query)
        }

        deleted > 0
    }

    suspend fun getTaskCount(projectId: Long): Int = withContext(Dispatchers.Default) {
        database.transaction {
            val sql = """
                SELECT COUNT(*) FROM task_projects
                WHERE project_id = ?
            """.trimIndent()

            var count = 0
            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, projectId)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            count = rs.getInt(1)
                        }
                    }
                }
            }
            count
        }
    }

    private fun mapProject(rs: ResultSet): Project {
        return Project(
            id = rs.getLong("id"),
            name = rs.getString("name"),
            description = rs.getString("description"),
            color = rs.getString("color"),
            isArchived = rs.getBoolean("is_archived"),
            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at"))
        )
    }
}
