// examples/example-multiplatform/src/iosMain/kotlin/com/korm/examples/multiplatform/models/IOSExtensions.kt

package com.korm.examples.multiplatform.models

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970

/**
 * iOS-specific extensions for Task model.
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

fun Task.priorityColorHex(): String {
    return when (priority) {
        TaskPriority.LOW -> "#4CAF50"
        TaskPriority.MEDIUM -> "#2196F3"
        TaskPriority.HIGH -> "#FF9800"
        TaskPriority.URGENT -> "#F44336"
    }
}

fun Task.statusColorHex(): String {
    return when (status) {
        TaskStatus.TODO -> "#9E9E9E"
        TaskStatus.IN_PROGRESS -> "#2196F3"
        TaskStatus.DONE -> "#4CAF50"
        TaskStatus.CANCELLED -> "#F44336"
    }
}
