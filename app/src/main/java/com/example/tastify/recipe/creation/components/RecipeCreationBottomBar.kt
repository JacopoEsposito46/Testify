package com.example.tastify.recipe.creation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.outlined.SaveAlt

@Composable
fun RecipeCreationBottomBar(
    isEditing: Boolean = false,
    onPublishClick: () -> Unit = {},
    onSavePrivateClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Card(
        colors = cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),
        elevation = CardDefaults.cardElevation(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDeleteClick),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "cancel",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (!isEditing) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onSavePrivateClick),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SaveAlt,
                        contentDescription = "save",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onPublishClick),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Upload,
                    contentDescription = "publish",
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEditing) "Save Changes" else "Publish",
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}