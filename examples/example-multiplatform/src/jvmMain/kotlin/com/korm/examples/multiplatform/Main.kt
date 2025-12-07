// examples/example-multiplatform/src/jvmMain/kotlin/com/korm/examples/multiplatform/Main.kt

package com.korm.examples.multiplatform

import com.korm.examples.multiplatform.models.*
import com.korm.examples.multiplatform.repositories.JVMProjectRepository
import com.korm.examples.multiplatform.repositories.JVMTaskRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlin.time.Duration.Companion.days

/**
 * JVM Console Application demonstrating KORM-DSL multiplatform usage.
 *
 * This example shows how the same KORM-DSL code works across platforms.
 */
fun main() = runBlocking {
    println("╔════════════════════════════════════════════════════╗")
    println("║   KORM Multiplatform Example - JVM Console App    ║")
    println("╚════════════════════════════════════════════════════╝")
    println()

    // Initialize repositories
    val taskRepository = JVMTaskRepository.create()
    val projectRepository = JVMProjectRepository.create()

    // Demo: Create Projects
    println("📁 Creating Projects...")
    val workProject = projectRepository.createProject(
        name = "Work",
        description = "Work-related tasks",
        color = "#2196F3"
    )
    val personalProject = projectRepository.createProject(
        name = "Personal",
        description = "Personal goals and tasks",
        color = "#4CAF50"
    )
    println("✓ Created ${workProject.name} project")
    println("✓ Created ${personalProject.name} project")
    println()

    // Demo: Create Tasks
    println("📝 Creating Tasks...")
    val task1 = taskRepository.createTask(
        title = "Complete KORM documentation",
        description = "Write comprehensive documentation for KORM-DSL",
        priority = TaskPriority.HIGH,
        status = TaskStatus.IN_PROGRESS,
        dueDate = Clock.System.now().plus(7.days),
        projectId = workProject.id
    )

    val task2 = taskRepository.createTask(
        title = "Review pull requests",
        description = "Review and merge pending PRs",
        priority = TaskPriority.MEDIUM,
        status = TaskStatus.TODO,
        dueDate = Clock.System.now().plus(3.days),
        projectId = workProject.id
    )

    val task3 = taskRepository.createTask(
        title = "Exercise routine",
        description = "Daily 30-minute workout",
        priority = TaskPriority.MEDIUM,
        status = TaskStatus.TODO,
        projectId = personalProject.id
    )

    println("✓ Created task: ${task1.title}")
    println("✓ Created task: ${task2.title}")
    println("✓ Created task: ${task3.title}")
    println()

    // Demo: Display All Tasks
    println("═══════════════════════════════════════════════════")
    println("📋 All Tasks:")
    println("═══════════════════════════════════════════════════")
    val allTasks = taskRepository.findAll()
    allTasks.forEach { task ->
        displayTask(task)
    }
    println()

    // Demo: Filter by Status
    println("═══════════════════════════════════════════════════")
    println("🔍 Tasks with status: IN_PROGRESS")
    println("═══════════════════════════════════════════════════")
    val inProgressTasks = taskRepository.findByStatus(TaskStatus.IN_PROGRESS)
    inProgressTasks.forEach { task ->
        displayTask(task)
    }
    println()

    // Demo: Filter by Priority
    println("═══════════════════════════════════════════════════")
    println("⚡ High Priority Tasks:")
    println("═══════════════════════════════════════════════════")
    val highPriorityTasks = taskRepository.findByPriority(TaskPriority.HIGH)
    highPriorityTasks.forEach { task ->
        displayTask(task)
    }
    println()

    // Demo: Search Tasks
    println("═══════════════════════════════════════════════════")
    println("🔎 Searching for 'KORM':")
    println("═══════════════════════════════════════════════════")
    val searchResults = taskRepository.searchByTitle("KORM")
    searchResults.forEach { task ->
        displayTask(task)
    }
    println()

    // Demo: Tasks by Project
    println("═══════════════════════════════════════════════════")
    println("📂 Tasks in 'Work' Project:")
    println("═══════════════════════════════════════════════════")
    val workTasks = taskRepository.findByProject(workProject.id)
    workTasks.forEach { task ->
        displayTask(task)
    }
    println()

    // Demo: Update Task
    println("✏️  Updating task status...")
    val updatedTask = taskRepository.updateStatus(task1.id, TaskStatus.DONE)
    println("✓ Updated '${updatedTask.title}' status to ${updatedTask.status}")
    println()

    // Demo: Statistics
    println("═══════════════════════════════════════════════════")
    println("📊 Task Statistics:")
    println("═══════════════════════════════════════════════════")
    val stats = taskRepository.getStatistics()
    println("Total Tasks:      ${stats.total}")
    println("TODO:             ${stats.todo}")
    println("IN PROGRESS:      ${stats.inProgress}")
    println("DONE:             ${stats.done}")
    println("CANCELLED:        ${stats.cancelled}")
    println()

    // Demo: Project Statistics
    println("═══════════════════════════════════════════════════")
    println("📁 Project Statistics:")
    println("═══════════════════════════════════════════════════")
    val projects = projectRepository.findAll()
    projects.forEach { project ->
        val taskCount = projectRepository.getTaskCount(project.id)
        println("${project.name}: $taskCount tasks")
    }
    println()

    // Demo: Delete Task
    println("🗑️  Deleting a task...")
    val deleted = taskRepository.deleteTask(task3.id)
    if (deleted) {
        println("✓ Successfully deleted '${task3.title}'")
    }
    println()

    println("╔════════════════════════════════════════════════════╗")
    println("║              Demo Completed Successfully!          ║")
    println("╚════════════════════════════════════════════════════╝")
}

fun displayTask(task: Task) {
    val priorityColor = task.priorityAnsiColor()
    val statusColor = task.statusAnsiColor()

    println("┌─────────────────────────────────────────────────")
    println("│ ${priorityColor}[${task.priority}]${ANSI_RESET} ${statusColor}[${task.status}]${ANSI_RESET} ${task.title}")
    task.description?.let {
        println("│ Description: $it")
    }
    task.dueDate?.let {
        println("│ Due: ${task.formattedDueDate()} ${if (task.isOverdue()) "⚠️ OVERDUE" else ""}")
    }
    println("│ Created: ${task.formattedCreatedAt()}")
    println("└─────────────────────────────────────────────────")
}
