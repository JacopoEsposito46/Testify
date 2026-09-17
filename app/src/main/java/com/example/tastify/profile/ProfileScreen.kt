package com.example.tastify.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.Recipe
import com.example.tastify.models.UserProfile
import com.example.tastify.profile.components.*
import com.example.tastify.profile.logic.ProfileViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    profile: UserProfile? = null,
    profileRecipes: List<Recipe> = emptyList(),
    isLoading: Boolean = false,
    isOwner: Boolean = true,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRecipeClick: (Recipe) -> Unit = {},
    isFollowing: Boolean = false,
    onFollowClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val user = profile ?: uiState.profile
    val displayUser = if (isOwner) {
        user.copy(
            topIngredient = uiState.topIngredient.ifBlank { user.topIngredient }
        )
    } else {
        user
    }
    val loading = isLoading || (!isOwner && profile == null)
    val topRecipes = profileRecipes
        .sortedByDescending { it.rating }
        .take(10)
    val recentRecipes = profileRecipes
        .sortedWith(
            compareByDescending<Recipe> { it.publicationDate.ifBlank { it.creationDate } }
                .thenByDescending { it.recipeId }
        )
        .take(10)


    Scaffold(
        topBar = {
            SharedTopBar(
                title = if (isOwner) "Personal information" else "Profile",
                onBack = onBack,
                actions = {
                    if(isOwner){
                        IconButton(onClick =
                            onEdit
                        ){
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            )
        },
    ) { innerPadding ->
        if (loading) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (isOwner) {
            BoxWithConstraints(
                modifier = Modifier.padding(innerPadding).fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val isLandscape = maxWidth > maxHeight
                val scrollState = rememberScrollState()
                val maxHeight = maxHeight

                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(maxHeight),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileImage(displayUser, 0.7f)
                            }
                            Column(
                                modifier = Modifier.weight(2f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProfileIdentity(displayUser)
                                Spacer(modifier = Modifier.height(16.dp))
                                ProfilePrivateStats(displayUser)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(maxHeight / 3),
                            contentAlignment = Alignment.Center
                        ) {
                            ProfileImage(displayUser, 0.8f)
                        }
                        ProfileIdentity(displayUser)
                        Spacer(modifier = Modifier.height(24.dp))
                        ProfilePrivateStats(displayUser)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileSecondaryStats(displayUser)
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileTagSection(
                        "Kitchen styles",
                        displayUser.favoritesCuisineType.map { it.displayName })
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileTagSection(
                        "Dietary/Restriction",
                        displayUser.selectedDietaryRestriction.map { it.displayName })
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileUsageCard(
                        totalUsageMinutes = uiState.totalUsageMinutes,
                        averageDailyUsageMinutes = uiState.averageDailyUsageMinutes,
                        weeklyUsageMinutes = uiState.weeklyUsageMinutes,
                        usageAccessGranted = uiState.usageAccessGranted,
                        onOpenUsageAccessSettings = {
                            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                        }
                    )
                }
            }
        } else {
            BoxWithConstraints(
                modifier = Modifier.padding(innerPadding).fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val isLandscape = maxWidth > maxHeight
                val scrollState = rememberScrollState()
                val maxHeight = maxHeight

                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLandscape) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(maxHeight),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileImage(user, 0.7f)
                            }
                            Column(
                                modifier = Modifier.weight(1.2f),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.Center
                            ) {
                                ProfileIdentity(user)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onFollowClick,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
                                        .fillMaxWidth(0.8f)
                                ) {
                                    Text(
                                        if (isFollowing) "Following" else "Follow",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                            }
                        }
                        //PAGINA DEL PROFILO VISTA DA UN ALTRO UTENTE
                    }
                    else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                ProfileImage(user, 0.8f)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(
                                modifier = Modifier.weight(1.5f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ProfileIdentity(user)

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = onFollowClick,
                                    modifier = Modifier
                                        .height(48.dp)
                                        .fillMaxWidth()
                                        .shadow(
                                            elevation = 12.dp,
                                            shape = RoundedCornerShape(20.dp),
                                            clip = false
                                        ),
                                    shape = RoundedCornerShape(20.dp),
                                ) {
                                    Text(
                                        if (isFollowing) "Following" else "Follow",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        ProfilePublicStats(user)

                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileRecipeCarouselSection(
                        title = "Top Recipes",
                        recipes = topRecipes,
                        emptyText = "No rated recipes yet",
                        onRecipeClick = onRecipeClick
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    ProfileRecipeCarouselSection(
                        title = "Recent Recipes",
                        recipes = recentRecipes,
                        emptyText = "No recipes published yet",
                        onRecipeClick = onRecipeClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ProfileRecipeCarouselSection(
    title: String,
    recipes: List<Recipe>,
    emptyText: String,
    onRecipeClick: (Recipe) -> Unit
) {
    ProfileSectionTitle(title = title)
    Spacer(modifier = Modifier.height(16.dp))

    if (recipes.isEmpty()) {
        Text(
            text = emptyText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
    } else {
        Carousel(items = recipes) { recipe ->
            DetailedRecipeCard(
                title = recipe.title,
                imageId = recipe.recipePhotoId,
                calories = recipe.calories,
                time = recipe.cookTime,
                modifier = Modifier.clickable { onRecipeClick(recipe) }
            )
        }
    }
}
