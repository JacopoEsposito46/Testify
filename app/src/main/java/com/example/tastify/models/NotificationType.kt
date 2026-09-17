package com.example.tastify.models

enum class NotificationType(val defaultTitle: String) {
    DUPLICATION("Your recipe was cloned!"),
    RECOMMENDATION("Recipe recommendation"),
    REVIEW_RECEIVED("New review received"),
    COMMENT_RECEIVED("New comment received")
}