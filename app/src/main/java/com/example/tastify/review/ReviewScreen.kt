package com.example.tastify.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastify.components.SharedTopBar
import com.example.tastify.review.components.ReviewItemCard
import com.example.tastify.review.components.ReviewSummaryCard
import com.example.tastify.review.logic.ReviewEvent
import com.example.tastify.review.logic.ReviewViewModel
import com.example.tastify.utils.SessionManager

@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onBack: () -> Unit,
    onWriteReview: () -> Unit,
    onNavigateToProfile: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recipe by viewModel.recipe.collectAsStateWithLifecycle()
    val isOwner = recipe?.authorId == SessionManager.CURRENT_LOGGED_IN_USER_ID

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Reviews",
                onBack = onBack
            )
        },
        bottomBar = {
            if(recipe != null && !isOwner) {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onWriteReview,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                        Text("Write Review", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                item {
                    ReviewSummaryCard(reviews = state.reviews)
                }
                items(state.reviews) { review ->
                    ReviewItemCard(
                        review = review,
                        isAuthor = isOwner,
                        recipeAuthorName = state.recipeAuthorNickname,
                        recipeAuthorAvatar = state.recipeAuthorProfileImageUri,
                        recipeAuthorId = recipe?.authorId ?: "",
                        onUserClick = onNavigateToProfile,
                        onReplySubmit = { replyText: String? ->
                            viewModel.onEvent(ReviewEvent.SubmitAuthorReply(review.reviewId, replyText))
                        }
                    )
                }
            }
        }
    }
}