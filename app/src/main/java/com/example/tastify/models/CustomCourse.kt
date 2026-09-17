package com.example.tastify.models

import java.util.UUID

data class CustomCourse(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val author: String = "",
    val recipeIds: List<String> = emptyList()
)
