package com.example.tastify.models

import java.util.UUID

data class RecipeIngredient(
    val id : String = UUID.randomUUID().toString(),
    val ingredient : Ingredient = Ingredient(),
    val quantity : Double = 0.0,
    val unit : String = ""
)
