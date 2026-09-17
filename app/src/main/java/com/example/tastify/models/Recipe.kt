package com.example.tastify.models

import com.google.firebase.firestore.PropertyName
import java.util.UUID

data class Recipe(
    val recipeId : String = UUID.randomUUID().toString(),
    val recipePhotoId : String = "",
    val title : String = "",
    val description : String = "",

    val cookTime : Int = 0,
    val difficulty : RecipeDifficulty = RecipeDifficulty.EASY,
    val calories : Int = 0,
    val serves : Int = 0,
    val cost : Int = 1,

    val ingredients : List<RecipeIngredient> = emptyList(),
    val cuisineType : CuisineType = CuisineType.DEFAULT,
    val dietaryRestrictions : List<DietaryRestriction> = emptyList(),

    val steps : List<RecipeStep> = emptyList(),
    val views : Int = 0,
    val favouriteCount : Int = 0,
    val commentCount : Int = 0,
    val reviewCount : Int = 0,

    val creationDate: String = "",
    val publicationDate: String = "",

    val authorId : String = "",
    val authorNickname : String = "",
    val rating : Double = 0.0,

    val reportCount: Int = 0,

    @get:PropertyName("isPublic")
    @set:PropertyName("isPublic")
    var isPublic: Boolean = true,
    val source : String = SOURCE_CREATED,
) {
    companion object {
        const val SOURCE_CREATED = "created"
        const val SOURCE_IMPORTED = "imported"
    }
}