// examples/example-multiplatform/src/jvmMain/kotlin/com/korm/examples/multiplatform/models/JVMExtensions.kt

package com.korm.examples.multiplatform.models

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

/**
 * JVM-specific extensions for Task model.
 */

fun Task.formattedDueDate(): String? {
    return dueDate?.let {
        val localDateTime = it.toLocalDateTime(TimeZone.currentSystemDefault())
        "${localDateTime.month.name.take(3)} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
    }
}

fun Task.formattedCreatedAt(): String {
    val localDateTime = createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.month.name.take(3)} ${localDateTime.dayOfMonth}, ${localDateTime.year}"
}

fun Task.isOverdue(): Boolean {
    return dueDate?.let { it < kotlinx.datetime.Clock.System.now() } ?: false
}

fun Task.priorityAnsiColor(): String {
    return when (priority) {
        TaskPriority.LOW -> "\u001B[32m" // Green
        TaskPriority.MEDIUM -> "\u001B[34m" // Blue
        TaskPriority.HIGH -> "\u001B[33m" // Yellow
        TaskPriority.URGENT -> "\u001B[31m" // Red
    }
}

fun Task.statusAnsiColor(): String {
    return when (status) {
        TaskStatus.TODO -> "\u001B[37m" // White
        TaskStatus.IN_PROGRESS -> "\u001B[34m" // Blue
        TaskStatus.DONE -> "\u001B[32m" // Green
        TaskStatus.CANCELLED -> "\u001B[31m" // Red
    }
}

const val ANSI_RESET = "\u001B[0m"
