package com.example.tastify.recipe.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.components.SharedBottomNavBar
import com.example.tastify.recipe.courses.components.CourseCard
import com.example.tastify.recipe.courses.components.CourseCreationSection
import com.example.tastify.recipe.courses.logic.CustomCoursesState
import com.example.tastify.components.SharedTopBar

@Composable
fun CustomCourses(
    state: CustomCoursesState,
    onBack: () -> Unit,
    onNavigateToRecipeProposalList: () -> Unit,
    onNavigateToMyProfile: () -> Unit,
    onNavigateToCookBook: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    unreadNotificationCount: Int = 0,
    onNavigateToRecipeList: (String?) -> Unit,
    onCreateCourse: (String) -> Unit,
    onDeleteClick: (String) -> Unit
){

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            SharedTopBar(
                title = "Your Courses",
                onBack = onBack,
                actions = {}
        )},
        bottomBar = {
            SharedBottomNavBar (
                selectedRoute = "cookbook",
                onRouteSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToRecipeProposalList()
                        "profile" -> onNavigateToMyProfile()
                        "cookbook" -> onNavigateToCookBook()
                        "updates" -> onNavigateToNotifications()
                        "chat" -> onNavigateToChat()
                        else -> {}
                    }
                },
                unreadNotificationCount = unreadNotificationCount
            )
        }
        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //favourite
            CourseCard(
                icon = Icons.Filled.Star,
                title = "My Favourite",
                recipeCount = state.favoriteCount,
                onClick = { onNavigateToRecipeList(null) },
                onDeleteClick = null
            )

            //imported
            CourseCard(
                icon = Icons.Filled.CloudDownload,
                title = "Imported Recipes",
                recipeCount = state.importedCount,
                onClick = { onNavigateToRecipeList("imported") },
                onDeleteClick = null
            )

            //custom
            state.courses.forEach { course ->
                CourseCard(
                    icon = Icons.Default.Menu,
                    title = course.title,
                    recipeCount = course.recipeIds.size,
                    onClick = { onNavigateToRecipeList(course.id) },
                    onDeleteClick = { onDeleteClick(course.id) }
                )
            }

            //action buttons
            CourseCreationSection(onCreateCourse)
        }
    }
}