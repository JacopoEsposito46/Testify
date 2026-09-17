package com.example.tastify.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.UUID

@IgnoreExtraProperties
data class UserProfile(
    val internalID : String = UUID.randomUUID().toString(),
    var username: String = "", //nickname
    var name : String = "",
    var surname : String = "",
    var email : String = "",
    var follower : Int = 0,
    var followedId : List<String> = emptyList(),
    var postedRecipes : Int = 0,
    var cookedRecipes : Int = 0,
    var topIngredient: String = "",
    var customCourses: List<List<Recipe>> = emptyList(),
    var favoritesCuisineType: List<CuisineType> = emptyList(),
    var selectedDietaryRestriction: List<DietaryRestriction> = emptyList(),
    var cookingRole: CookingRole = CookingRole.BEGINNER,
    var notificationsEnabled: Boolean = true,

    var profilePictureUrl: String? = null,
    var experiencePoints: Int = 0,
    var chosenFrame: ProfileFrame = ProfileFrame.BRONZE,

    @get:Exclude
    @set:Exclude
    var profilePicture: ImageSource? = null,
    )
