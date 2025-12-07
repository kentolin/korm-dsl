// examples/example-multiplatform/src/androidMain/kotlin/com/korm/examples/multiplatform/models/AndroidExtensions.kt

package com.korm.examples.multiplatform.models

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android-specific extensions for Task model.
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

fun Task.priorityColor(): Long {
    return when (priority) {
        TaskPriority.LOW -> 0xFF4CAF50
        TaskPriority.MEDIUM -> 0xFF2196F3
        TaskPriority.HIGH -> 0xFFFF9800
        TaskPriority.URGENT -> 0xFFF44336
    }
}

fun Task.statusColor(): Long {
    return when (status) {
        TaskStatus.TODO -> 0xFF9E9E9E
        TaskStatus.IN_PROGRESS -> 0xFF2196F3
        TaskStatus.DONE -> 0xFF4CAF50
        TaskStatus.CANCELLED -> 0xFFF44336
    }
}
