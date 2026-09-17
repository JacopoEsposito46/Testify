package com.example.tastify.models

import kotlinx.serialization.Serializable

@Serializable
data class ExtractedRecipeData(
    val url: String,
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val serves: Int? = null,
    val calories: Int? = null
)