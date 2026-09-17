package com.example.tastify.recipe.creation.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeDifficulty
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationStats(
    recipe: Recipe,
    onValueChange: (Recipe) -> Unit = {},
    timeError: String? = null,
    caloriesError: String? = null,
    servesError: String? = null,
    onClearTimeError: () -> Unit = {},
    onClearCaloriesError: () -> Unit = {},
    onClearServesError: () -> Unit = {},
){
    var difficultyMenuExpanded by remember { mutableStateOf(false) }
    val difficultyOptions = RecipeDifficulty.entries

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(),
        border = CardDefaults.outlinedCardBorder()
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Cook time
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = "cookTime",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cook time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = if(recipe.cookTime == 0) "" else recipe.cookTime.toString(),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            val cookTimeInt = newValue.toIntOrNull() ?: 0
                            onValueChange(recipe.copy(cookTime = cookTimeInt))
                            onClearTimeError()
                        }
                    },
                    isError = timeError != null,
                    supportingText = {
                        if (timeError != null) {
                            Text(
                                text = timeError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    suffix = {
                        Text(
                            text = " min"
                        )},
                    placeholder = {
                        Text(
                            text = "e.g. 30",
                            style = MaterialTheme.typography.bodyMedium
                        )},
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            //Difficulty
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Analytics,
                        contentDescription = "difficulty",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Difficulty",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                ExposedDropdownMenuBox(
                    expanded = difficultyMenuExpanded,
                    onExpandedChange = { }
                ) {
                    OutlinedTextField(
                        value = recipe.difficulty.displayName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable, true),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                difficultyMenuExpanded = !difficultyMenuExpanded
                            }) {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyMenuExpanded)
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                    ExposedDropdownMenu(
                        expanded = difficultyMenuExpanded,
                        onDismissRequest = { difficultyMenuExpanded = false }
                    ) {
                        difficultyOptions.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.displayName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                onClick = {
                                    onValueChange(recipe.copy(difficulty = option))
                                    difficultyMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            //Calories
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = "calories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = if(recipe.calories == 0) "" else recipe.calories.toString(),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            val caloriesInt = newValue.toIntOrNull() ?: 0
                            onValueChange(recipe.copy(calories = caloriesInt))
                            onClearCaloriesError()
                        }
                    },
                    isError = caloriesError != null,
                    supportingText = {
                        if (caloriesError != null) {
                            Text(
                                text = caloriesError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    suffix = {
                        Text(
                            text = " kcal",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    placeholder = {
                        Text(
                            text = "e.g. 400",
                            style = MaterialTheme.typography.bodyMedium
                        )},
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }

            //Serves
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "serves",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Serves",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                OutlinedTextField(
                    value = if(recipe.serves == 0) "" else recipe.serves.toString(),
                    onValueChange = { newValue ->
                        if (newValue.all { it.isDigit() }) {
                            val servesInt = newValue.toIntOrNull() ?: 0
                            onValueChange(recipe.copy(serves = servesInt))
                            onClearServesError()
                        }
                    },
                    isError = servesError != null,
                    supportingText = {
                        if (servesError != null) {
                            Text(
                                text = servesError,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "e.g. 3",
                            style = MaterialTheme.typography.bodyMedium
                        )},
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
            }
        }
    }

    //Cost range
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AttachMoney,
                contentDescription = "cost range",
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Cost range",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.weight(1f))
            repeat(recipe.cost){
                Icon(
                    imageVector = Icons.Outlined.AttachMoney,
                    contentDescription = "cost range",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Slider(
            value = recipe.cost.toFloat(),
            onValueChange = { newValue ->
                onValueChange(recipe.copy(cost = newValue.toInt()))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            colors = SliderDefaults.colors(
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary,
                activeTickColor = MaterialTheme.colorScheme.primary,
            ),
            steps = 3,
            valueRange = 1f..5f,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
            }
        )
    }
}