package com.example.tastify.models

import java.util.UUID

data class Report(
    val reportId: String = UUID.randomUUID().toString(),
    val recipeId: String = "",
    val authorId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
)
