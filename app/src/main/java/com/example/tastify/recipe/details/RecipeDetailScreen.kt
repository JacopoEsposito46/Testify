package com.example.tastify.recipe.details

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ShapeDefaults.Medium
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.tastify.recipe.common.RecipeDetailMenu
import com.example.tastify.recipe.details.components.RecipeInfo
import com.example.tastify.recipe.details.components.RecipeIngredients
import com.example.tastify.recipe.details.components.RecipeStats
import com.example.tastify.recipe.details.components.RecipeSteps
import com.example.tastify.components.SharedTopBar
import com.example.tastify.recipe.details.logic.RecipeDetailEvent
import com.example.tastify.recipe.details.logic.RecipeDetailViewModel
import com.example.tastify.ui.theme.Amber
import com.example.tastify.utils.PdfThemeColors

@Composable
fun RecipeDetailScreen(
    viewModel: RecipeDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToComment: () -> Unit,
    onNavigateToReview: () -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToOtherProfile: (String) -> Unit,
    onNavigateToEditRecipeProposal: () -> Unit,
    onNavigateToCopyRecipeProposal: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val recipe = state.selectedRecipe
    val isOwner = state.isOwner
    var showMenu by remember { mutableStateOf(false) }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAddToListDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.onEvent(RecipeDetailEvent.RecipeOpened)
    }

    val pdfColors = PdfThemeColors(
        primary = MaterialTheme.colorScheme.primary.toArgb(),
        primaryLight = MaterialTheme.colorScheme.surfaceVariant.toArgb(),
        text = MaterialTheme.colorScheme.onSurface.toArgb(),
        textGray = MaterialTheme.colorScheme.onSurfaceVariant.toArgb(),
        divider = MaterialTheme.colorScheme.outlineVariant.toArgb()
    )

    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.onEvent(RecipeDetailEvent.DownloadRecipePdf(uri, pdfColors))
        }
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            SharedTopBar(
                title = "Recipe Details",
                onBack = onNavigateBack,
                actions = {
                    if(recipe != null) {
                        if(!isOwner) {
                            IconButton(onClick = {
                                viewModel.onEvent(RecipeDetailEvent.ToggleFavourite)
                            }){
                                Icon(
                                    imageVector = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                                    contentDescription = "favorite",
                                    tint = if (state.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            if (state.isLoadingDetails) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (recipe == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Recipe not found", style = MaterialTheme.typography.headlineMedium)
                }
            } else {
                var currentServes by rememberSaveable { mutableIntStateOf(recipe.serves) }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (recipe.recipePhotoId.isNotBlank()) {
                        Box {
                            AsyncImage(
                                model = recipe.recipePhotoId,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(350.dp).clip(shape = Medium)
                            )
                            //Rating
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 8.dp, top = 8.dp),
                                shape = CircleShape,
                                border = CardDefaults.outlinedCardBorder(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = "rating",
                                        tint = Amber
                                    )
                                    Text(
                                        recipe.rating.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Card(
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(top = 280.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                RecipeInfo(
                                    recipe = recipe,
                                    onNavigateToProfile = onNavigateToProfile,
                                    onNavigateToOtherProfile = onNavigateToOtherProfile
                                )
                                RecipeStats(
                                    recipe = recipe,
                                    currentServes = currentServes,
                                    isOwner = isOwner,
                                    onIncrease = { if (currentServes < 10) currentServes++ },
                                    onDecrease = { if (currentServes > 2) currentServes-- }
                                )
                            }
                        }
                    } else {
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    RecipeInfo(
                                        recipe = recipe,
                                        onNavigateToProfile = onNavigateToProfile,
                                        onNavigateToOtherProfile = onNavigateToOtherProfile
                                    )
                                    RecipeStats(
                                        recipe = recipe,
                                        currentServes = currentServes,
                                        isOwner = isOwner,
                                        onIncrease = { if (currentServes < 10) currentServes++ },
                                        onDecrease = { if (currentServes > 2) currentServes-- }
                                    )
                                }
                                //Rating
                                Card(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(end = 8.dp, top = 8.dp),
                                    shape = CircleShape,
                                    border = CardDefaults.outlinedCardBorder(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "rating",
                                            tint = Amber
                                        )
                                        Text(
                                            recipe.rating.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }



                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Box(modifier = Modifier.width(6.dp).height(20.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Ingredients", style = MaterialTheme.typography.headlineMedium)
                    }
                    RecipeIngredients(recipe = recipe, currentServes = currentServes)

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Box(modifier = Modifier.width(6.dp).height(20.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Preparation Steps", style = MaterialTheme.typography.headlineMedium)
                    }
                    RecipeSteps(recipe = recipe)
                    Spacer(modifier = Modifier.height(100.dp))
                }
                RecipeDetailMenu(
                    isVisible = showMenu,
                    isOwner = isOwner,
                    isPublic = recipe.isPublic,
                    onDismiss = { showMenu = false },
                    onReviewClick = onNavigateToReview,
                    onCommentClick = onNavigateToComment,
                    onReportClick = onNavigateToReport,
                    onStatsClick = onNavigateToStats,
                    onDownloadClick = {
                        val fileName = "${recipe.title.replace(" ", "_")}.pdf"
                        pdfLauncher.launch(fileName)
                    },
                    onSaveClick = { showAddToListDialog = true },
                    isDone = state.isDone,
                    onDoneClick = {
                        viewModel.onEvent(RecipeDetailEvent.ToggleDone)
                    },
                    onModifyClick = {
                        if (isOwner) {
                            onNavigateToEditRecipeProposal()
                        } else {
                            onNavigateToCopyRecipeProposal()
                        }
                    },
                    onDeleteClick = {
                        showDeleteDialog = true
                    },
                    onPublishClick = {
                        viewModel.onEvent(RecipeDetailEvent.PublishRecipe)
                    },
                    onUnpublishClick = {
                        viewModel.onEvent(RecipeDetailEvent.UnpublishRecipe)
                    }
                )
            }
        }
    }

    if (showDeleteDialog && recipe != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete recipe") },
            text = { Text("Are you sure you want to delete this recipe?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.onEvent(RecipeDetailEvent.DeleteRecipe(recipe.recipeId))
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if(showAddToListDialog && recipe != null) {
        AlertDialog(
            onDismissRequest = { showAddToListDialog = false },
            title = { Text("Save to Course") },
            text = {
                if (state.courses.isEmpty()) {
                    Text("You don't have any courses yet")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.courses) { course ->
                            Card(
                                onClick = {
                                    viewModel.onEvent(RecipeDetailEvent.AddToCourse(course.id, recipe.recipeId))
                                    showAddToListDialog = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = course.title,
                                    modifier = Modifier
                                        .padding(16.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAddToListDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}