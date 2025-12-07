// examples/example-multiplatform/src/commonMain/kotlin/com/korm/examples/multiplatform/repositories/TaskRepository.kt

package com.korm.examples.multiplatform.repositories

import com.korm.dsl.core.Database
import com.korm.examples.multiplatform.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import java.sql.ResultSet

/**
 * Task repository - shared business logic across all platforms.
 *
 * Demonstrates KORM-DSL CRUD operations in a multiplatform context.
 */
class TaskRepository(private val database: Database) {

    /**
     * Create a new task.
     *
     * KORM-DSL Usage: insertInto() with transaction
     */
    suspend fun createTask(
        title: String,
        description: String?,
        priority: TaskPriority,
        status: TaskStatus = TaskStatus.TODO,
        dueDate: Instant? = null,
        projectId: Long? = null
    ): Task = withContext(Dispatchers.Default) {
        val taskId = database.transaction {
            val taskId = insertInto(Tasks) {
                it[Tasks.title] = title
                description?.let { desc -> it[Tasks.description] = desc }
                it[Tasks.priority] = priority.name
                it[Tasks.status] = status.name
                dueDate?.let { date -> it[Tasks.dueDate] = date.toEpochMilliseconds() }
            }.let { insert(it) }

            // Associate with project if provided
            projectId?.let { pid ->
                insertInto(TaskProjects) {
                    it[TaskProjects.taskId] = taskId
                    it[TaskProjects.projectId] = pid
                }.let { insert(it) }
            }

            taskId
        }

        findById(taskId)!!
    }

    /**
     * Find task by ID.
     *
     * KORM-DSL Usage: from().where() query
     */
    suspend fun findById(id: Long): Task? = withContext(Dispatchers.Default) {
        database.transaction {
            val query = from(Tasks).where(Tasks.id eq id)
            val results = select(query, ::mapTask)
            results.firstOrNull()
        }
    }

    /**
     * Find all tasks.
     *
     * KORM-DSL Usage: from() with orderBy
     */
    suspend fun findAll(): List<Task> = withContext(Dispatchers.Default) {
        database.transaction {
            val query = from(Tasks).orderBy(Tasks.createdAt, "DESC")
            select(query, ::mapTask)
        }
    }

    /**
     * Find tasks by status.
     *
     * KORM-DSL Usage: where() with enum comparison
     */
    suspend fun findByStatus(status: TaskStatus): List<Task> = withContext(Dispatchers.Default) {
        database.transaction {
            val query = from(Tasks)
                .where(Tasks.status eq status.name)
                .orderBy(Tasks.dueDate)
            select(query, ::mapTask)
        }
    }

    /**
     * Find tasks by priority.
     *
     * KORM-DSL Usage: where() with filtering
     */
    suspend fun findByPriority(priority: TaskPriority): List<Task> = withContext(Dispatchers.Default) {
        database.transaction {
            val query = from(Tasks)
                .where(Tasks.priority eq priority.name)
                .orderBy(Tasks.dueDate)
            select(query, ::mapTask)
        }
    }

    /**
     * Find tasks by project.
     *
     * KORM-DSL Usage: JOIN query with junction table
     */
    suspend fun findByProject(projectId: Long): List<Task> = withContext(Dispatchers.Default) {
        database.transaction {
            val sql = """
                SELECT t.*
                FROM tasks t
                INNER JOIN task_projects tp ON t.id = tp.task_id
                WHERE tp.project_id = ?
                ORDER BY t.created_at DESC
            """.trimIndent()

            val results = mutableListOf<Task>()
            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, projectId)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            results.add(mapTask(rs))
                        }
                    }
                }
            }
            results
        }
    }

    /**
     * Search tasks by title.
     *
     * KORM-DSL Usage: LIKE operator for text search
     */
    suspend fun searchByTitle(keyword: String): List<Task> = withContext(Dispatchers.Default) {
        database.transaction {
            val query = from(Tasks)
                .where(Tasks.title like "%$keyword%")
                .orderBy(Tasks.createdAt, "DESC")
            select(query, ::mapTask)
        }
    }

    /**
     * Get overdue tasks.
     *
     * KORM-DSL Usage: Complex where clause with date comparison
     */
    suspend fun findOverdue(): List<Task> = withContext(Dispatchers.Default) {
        val now = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()

        database.transaction {
            val sql = """
                SELECT * FROM tasks
                WHERE due_date < ? AND status != 'DONE' AND status != 'CANCELLED'
                ORDER BY due_date ASC
            """.trimIndent()

            val results = mutableListOf<Task>()
            getConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, now)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            results.add(mapTask(rs))
                        }
                    }
                }
            }
            results
        }
    }

    /**
     * Update task.
     *
     * KORM-DSL Usage: update() with partial updates
     */
    suspend fun updateTask(
        id: Long,
        title: String? = null,
        description: String? = null,
        priority: TaskPriority? = null,
        status: TaskStatus? = null,
        dueDate: Instant? = null
    ): Task = withContext(Dispatchers.Default) {
        database.transaction {
            val query = update(Tasks) {
                title?.let { t -> it[Tasks.title] = t }
                description?.let { d -> it[Tasks.description] = d }
                priority?.let { p -> it[Tasks.priority] = p.name }
                status?.let { s -> it[Tasks.status] = s.name }
                dueDate?.let { d -> it[Tasks.dueDate] = d.toEpochMilliseconds() }
                it[Tasks.updatedAt] = System.currentTimeMillis()
            }.where(Tasks.id eq id)

            update(query)
        }

        findById(id)!!
    }

    /**
     * Update task status.
     *
     * KORM-DSL Usage: Targeted update of single column
     */
    suspend fun updateStatus(id: Long, status: TaskStatus): Task = withContext(Dispatchers.Default) {
        database.transaction {
            val query = update(Tasks) {
                it[Tasks.status] = status.name
                it[Tasks.updatedAt] = System.currentTimeMillis()
            }.where(Tasks.id eq id)

            update(query)
        }

        findById(id)!!
    }

    /**
     * Delete task.
     *
     * KORM-DSL Usage: deleteFrom() with CASCADE handling
     */
    suspend fun deleteTask(id: Long): Boolean = withContext(Dispatchers.Default) {
        val deleted = database.transaction {
            // Delete associations first (if not using CASCADE)
            deleteFrom(TaskProjects) {
                where(TaskProjects.taskId eq id)
            }.let { delete(it) }

            // Delete task
            val query = deleteFrom(Tasks) {
                where(Tasks.id eq id)
            }
            delete(query)
        }

        deleted > 0
    }

    /**
     * Associate task with project.
     *
     * KORM-DSL Usage: Junction table insert
     */
    suspend fun addTaskToProject(taskId: Long, projectId: Long): Boolean = withContext(Dispatchers.Default) {
        try {
            database.transaction {
                insertInto(TaskProjects) {
                    it[TaskProjects.taskId] = taskId
                    it[TaskProjects.projectId] = projectId
                }.let { insert(it) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Remove task from project.
     *
     * KORM-DSL Usage: Junction table delete
     */
    suspend fun removeTaskFromProject(taskId: Long, projectId: Long): Boolean = withContext(Dispatchers.Default) {
        val deleted = database.transaction {
            val query = deleteFrom(TaskProjects) {
                where((TaskProjects.taskId eq taskId) and (TaskProjects.projectId eq projectId))
            }
            delete(query)
        }

        deleted > 0
    }

    /**
     * Get task statistics.
     *
     * KORM-DSL Usage: Aggregate queries
     */
    suspend fun getStatistics(): TaskStatistics = withContext(Dispatchers.Default) {
        database.transaction {
            val sql = """
                SELECT
                    COUNT(*) as total,
                    COUNT(CASE WHEN status = 'TODO' THEN 1 END) as todo,
                    COUNT(CASE WHEN status = 'IN_PROGRESS' THEN 1 END) as in_progress,
                    COUNT(CASE WHEN status = 'DONE' THEN 1 END) as done,
                    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled
                FROM tasks
            """.trimIndent()

            var stats = TaskStatistics(0, 0, 0, 0, 0)

            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            stats = TaskStatistics(
                                total = rs.getInt("total"),
                                todo = rs.getInt("todo"),
                                inProgress = rs.getInt("in_progress"),
                                done = rs.getInt("done"),
                                cancelled = rs.getInt("cancelled")
                            )
                        }
                    }
                }
            }
            stats
        }
    }

    /**
     * Map ResultSet to Task.
     */
    private fun mapTask(rs: ResultSet): Task {
        return Task(
            id = rs.getLong("id"),
            title = rs.getString("title"),
            description = rs.getString("description"),
            priority = TaskPriority.valueOf(rs.getString("priority")),
            status = TaskStatus.valueOf(rs.getString("status")),
            dueDate = rs.getLong("due_date").takeIf { it > 0 }?.let {
                Instant.fromEpochMilliseconds(it)
            },
            createdAt = Instant.fromEpochMilliseconds(rs.getLong("created_at")),
            updatedAt = Instant.fromEpochMilliseconds(rs.getLong("updated_at"))
        )
    }
}

data class TaskStatistics(
    val total: Int,
    val todo: Int,
    val inProgress: Int,
    val done: Int,
    val cancelled: Int
)
