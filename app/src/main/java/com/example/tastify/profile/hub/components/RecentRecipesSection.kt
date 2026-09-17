package com.example.tastify.profile.hub.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.tastify.models.Recipe
import com.example.tastify.profile.components.Carousel
import com.example.tastify.profile.components.RecentRecipeCard

@Composable
fun RecentRecipesSection(
    modifier: Modifier = Modifier,
    recipes: List<Recipe>,
    title: String = "Recent Recipes",
    onSeeMoreClick: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            TextButton(onClick = onSeeMoreClick) {
                Text(
                    text = "See more",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Carousel(
            items = recipes
        ) { recipe ->
            RecentRecipeCard(
                title = recipe.title,
                imageId = recipe.recipePhotoId,
                isPublic = recipe.isPublic,
                modifier = Modifier.clickable { onRecipeClick(recipe) }
            )
        }
    }
}