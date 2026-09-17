package com.example.tastify.models

import java.util.UUID

data class Ingredient (
    val ingredientId: String = UUID.randomUUID().toString(),
    val name: String = "",
    val defaultImage: String? = null
)