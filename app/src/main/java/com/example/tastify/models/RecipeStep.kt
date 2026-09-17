package com.example.tastify.models

data class RecipeStep(
    val stepNumber : Int = 0,
    val title : String = "",
    val description : String = "",
    val stepPhotoId : String = ""
)
