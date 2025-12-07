// examples/example-multiplatform/src/commonMain/kotlin/com/korm/examples/multiplatform/services/DatabaseInitializer.kt

package com.korm.examples.multiplatform.services

import com.korm.dsl.core.Database
import com.korm.examples.multiplatform.models.*

/**
 * Database initializer - shared across all platforms.
 *
 * Demonstrates KORM-DSL table creation and initialization.
 */
object DatabaseInitializer {

    fun initialize(database: Database) {
        // Create tables using KORM-DSL
        database.createTables(Projects, Tasks, TaskProjects)

        // Initialize with sample data if needed
        initializeSampleData(database)
    }

    private fun initializeSampleData(database: Database) {
        database.transaction {
            // Check if data already exists
            var hasData = false
            getConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT COUNT(*) FROM projects").use { rs ->
                        if (rs.next() && rs.getInt(1) > 0) {
                            hasData = true
                        }
                    }
                }
            }

            if (!hasData) {
                // Create sample projects
                val projectIds = listOf(
                    Triple("Personal", "Personal tasks and goals", "#4CAF50"),
                    Triple("Work", "Work-related tasks", "#2196F3"),
                    Triple("Learning", "Study and learning activities", "#9C27B0")
                ).map { (name, desc, color) ->
                    insertInto(Projects) {
                        it[Projects.name] = name
                        it[Projects.description] = desc
                        it[Projects.color] = color
                    }.let { insert(it) }
                }

                // Create sample tasks
                val sampleTasks = listOf(
                    Quad("Complete KORM documentation", "Write comprehensive docs", TaskPriority.HIGH, TaskStatus.IN_PROGRESS),
                    Quad("Review pull requests", "Review and merge PRs", TaskPriority.MEDIUM, TaskStatus.TODO),
                    Quad("Update dependencies", "Update project dependencies", TaskPriority.LOW, TaskStatus.TODO),
                    Quad("Learn Kotlin Multiplatform", "Study KMP best practices", TaskPriority.HIGH, TaskStatus.IN_PROGRESS),
                    Quad("Exercise", "Daily workout routine", TaskPriority.MEDIUM, TaskStatus.TODO)
                )

                sampleTasks.forEachIndexed { index, (title, desc, priority, status) ->
                    val taskId = insertInto(Tasks) {
                        it[Tasks.title] = title
                        it[Tasks.description] = desc
                        it[Tasks.priority] = priority.name
                        it[Tasks.status] = status.name
                    }.let { insert(it) }

                    // Associate task with project
                    val projectId = projectIds[index % projectIds.size]
                    insertInto(TaskProjects) {
                        it[TaskProjects.taskId] = taskId
                        it[TaskProjects.projectId] = projectId
                    }.let { insert(it) }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
