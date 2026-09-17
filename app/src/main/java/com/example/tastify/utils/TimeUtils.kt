package com.example.tastify.utils

private data class RelativeTimeParts(
    val minutes: Long,
    val hours: Long,
    val days: Long,
    val years: Long
)

private fun Long.relativeTimeParts(): RelativeTimeParts {
    val diff = System.currentTimeMillis() - this
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    val years = days / 365

    return RelativeTimeParts(
        minutes = minutes,
        hours = hours,
        days = days,
        years = years
    )
}

fun Long.toShortRelativeTime(): String {
    val parts = relativeTimeParts()

    return when {
        parts.years > 0 -> "${parts.years}y ago"
        parts.days > 0 -> "${parts.days}d ago"
        parts.hours > 0 -> "${parts.hours}h ago"
        parts.minutes > 0 -> "${parts.minutes}m ago"
        else -> "now"
    }
}

fun Long.toNotificationRelativeTime(): String {
    val parts = relativeTimeParts()

    return when {
        parts.minutes < 1 -> "Just now"
        parts.minutes < 60 -> "${parts.minutes}m ago"
        parts.hours < 24 -> "${parts.hours}h ago"
        parts.days < 7 -> "${parts.days}d ago"
        else -> "${parts.days / 7}w ago"
    }
}

fun Long.toHistoryRelativeTime(): String {
    val parts = relativeTimeParts()

    return when {
        parts.minutes < 1 -> "Just now"
        parts.minutes < 60 -> "${parts.minutes} m ago"
        parts.hours < 24 -> "${parts.hours} h ago"
        parts.days == 1L -> "Yesterday"
        else -> "${parts.days} days ago"
    }
}
