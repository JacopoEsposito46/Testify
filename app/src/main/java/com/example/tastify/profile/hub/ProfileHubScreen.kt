package com.example.tastify.profile.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastify.components.SharedBottomNavBar
import com.example.tastify.components.SharedTopBar
import com.example.tastify.models.Recipe
import com.example.tastify.models.UserProfile
import com.example.tastify.profile.hub.components.ProfileHubHeader
import com.example.tastify.profile.hub.components.ProfileHubMenuGrid
import com.example.tastify.profile.hub.components.RecentRecipesSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileHubScreen(
    userProfile: UserProfile?,
    recentRecipes: List<Recipe>,
    recentRecipesTitle: String = "Recent Recipes",
    isLoading: Boolean,
    onBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToPersonalInfo: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToPlanning: () -> Unit,
    onNavigateToRecipeProposalList: () -> Unit,
    onNavigateToCookBook: () -> Unit,
    onNavigateToRecipeList: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onRecipeClick: (Recipe) -> Unit,
    onAddRecipe: () -> Unit,
    onNavigateToImportRecipe: () -> Unit
) {
    var showAddRecipeSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Profile Hub",
                onBack = onBack,
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            SharedBottomNavBar(
                selectedRoute = "profile",
                onRouteSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToRecipeProposalList()
                        "cookbook" -> onNavigateToCookBook()
                        "updates" -> onNavigateToNotifications()
                        "chat" -> onNavigateToChat()
                    }
                },
                unreadNotificationCount = unreadNotificationCount
            )
        }
    ) { padding ->
        if (isLoading && userProfile == null && recentRecipes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileHubHeader(
                        userProfile = userProfile
                    )
                }

                item {
                    ProfileHubMenuGrid(
                        onNavigateToHistory = onNavigateToHistory,
                        onNavigateToPersonalInfo = onNavigateToPersonalInfo,
                        onNavigateToAnalytics = onNavigateToAnalytics,
                        onNavigateToPlanning = onNavigateToPlanning
                    )
                }

                item {
                    RecentRecipesSection(
                        recipes = recentRecipes,
                        title = recentRecipesTitle,
                        onSeeMoreClick = onNavigateToRecipeList,
                        onRecipeClick = onRecipeClick
                    )
                }

                item {
                    Button(
                        onClick = { showAddRecipeSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add New Recipe",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        if (showAddRecipeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddRecipeSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Create Manually", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {Text("Create manually your own recipe")},
                        leadingContent = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddRecipeSheet = false
                            onAddRecipe()
                        }
                    )
                    ListItem(
                        headlineContent = { Text("Import from Social", fontWeight = FontWeight.SemiBold) },
                        supportingContent = {Text("Import a recipe from social or URL")},
                        leadingContent = {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        trailingContent = {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable {
                            showAddRecipeSheet = false
                            onNavigateToImportRecipe()
                        }
                    )
                }
            }
        }
    }
}