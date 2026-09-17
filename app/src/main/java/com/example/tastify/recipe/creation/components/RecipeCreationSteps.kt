package com.example.tastify.recipe.creation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeStep
import com.example.tastify.recipe.dashedBorder
import com.example.tastify.ui.theme.TertiaryOrange
import com.example.tastify.ui.theme.White
import kotlin.collections.plus

@Suppress("DEPRECATION")
@Composable
fun RecipeCreationSteps(
    recipe: Recipe,
    onValueChange: (Recipe) -> Unit = {},
    stepsError: String? = null,
    stepImageErrors: Map<Int, String> = emptyMap(),
    stepTitleErrors: Map<Int, String> = emptyMap(),
    stepDescriptionErrors: Map<Int, String> = emptyMap(),
    onClearStepsError: () -> Unit = {},
    onClearStepImageError: (Int) -> Unit = {},
    onClearStepTitleError: (Int) -> Unit = {},
    onClearStepDescriptionError: (Int) -> Unit = {}
){
    var isDeleteMode by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ){
        Text(
            text = "Steps",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .align(Alignment.Start)
        )

        if (stepsError != null) {
            Text(
                text = stepsError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        }

        recipe.steps.forEachIndexed { index, step ->
            val imageError = stepImageErrors[index]
            val titleError = stepTitleErrors[index]
            val descriptionError = stepDescriptionErrors[index]

            Card(
                colors = cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(),
                border = CardDefaults.outlinedCardBorder()
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                    ImagePicker(
                        imageUri = step.stepPhotoId,
                        isCover = false,
                        modifier = Modifier
                            .width(100.dp)
                            .height(120.dp),
                        isError = imageError != null,
                        errorMessage = imageError,
                        onImageSelected = { newUri ->
                            val newSteps = recipe.steps.toMutableList()
                            newSteps[index] = newSteps[index].copy(stepPhotoId = newUri)
                            onValueChange(recipe.copy(steps = newSteps))
                            onClearStepImageError(index)
                        }
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ){
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            //step number
                            Card(
                                colors = cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                elevation = CardDefaults.cardElevation(),
                                border = CardDefaults.outlinedCardBorder()
                            ) {
                                Text(
                                    text = "Step ${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            if (isDeleteMode) {
                                IconButton(
                                    onClick = {
                                        val newList = recipe.steps.toMutableList()
                                        if (newList.size > 1) {
                                            newList.removeAt(index)
                                            val renumberedSteps = newList.mapIndexed { i, s -> s.copy(stepNumber = i + 1) }
                                            onValueChange(recipe.copy(steps = renumberedSteps))
                                        } else {
                                            newList.removeAt(index)
                                            onValueChange(recipe.copy(steps = newList))
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "Remove ingredient",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }

                        //title
                        OutlinedTextField(
                            value = step.title,
                            onValueChange = { newValue ->
                                val newList = recipe.steps.toMutableList()
                                newList[index] = newList[index].copy(title = newValue)
                                onValueChange(recipe.copy(steps = newList))
                                onClearStepTitleError(index)
                                onClearStepsError()
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            isError = titleError != null,
                            supportingText = {
                                if (titleError != null) {
                                    Text(
                                        text = titleError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            trailingIcon = {
                                if (step.title.isNotEmpty()) {
                                    IconButton(onClick = {
                                        val newList = recipe.steps.toMutableList()
                                        newList[index] = newList[index].copy(title = "")
                                        onValueChange(recipe.copy(steps = newList))
                                    }){
                                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = "Step title",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            shape = MaterialTheme.shapes.medium
                        )

                        //description
                        OutlinedTextField(
                            value = step.description,
                            onValueChange = { newValue ->
                                val newList = recipe.steps.toMutableList()
                                newList[index] = newList[index].copy(description = newValue)
                                onValueChange(recipe.copy(steps = newList))
                                onClearStepDescriptionError(index)
                                onClearStepsError()
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            isError = descriptionError != null,
                            supportingText = {
                                if (descriptionError != null) {
                                    Text(
                                        text = descriptionError,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            trailingIcon = {
                                if (step.description.isNotEmpty()) {
                                    IconButton(onClick = {
                                        val newList = recipe.steps.toMutableList()
                                        newList[index] = newList[index].copy(description = "")
                                        onValueChange(recipe.copy(steps = newList))
                                    }){
                                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            label = {
                                Text(
                                    text = "Step description",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            shape = MaterialTheme.shapes.medium
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ){
            //Add step button
            Card(
                modifier = Modifier
                    .clickable(onClick = {
                        val newList = recipe.steps + RecipeStep(stepNumber = recipe.steps.size + 1)
                        onValueChange(recipe.copy(steps = newList))
                    })
                    .dashedBorder(
                        2.dp,
                        TertiaryOrange,
                        MaterialTheme.shapes.medium,
                        10.dp,
                        4.dp
                    )
                    .weight(1f),
                shape = MaterialTheme.shapes.medium,
                colors = cardColors(White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add step",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Add step",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            //Remove step button
            Card(
                modifier = Modifier
                    .clickable(
                        enabled = recipe.steps.size > 1,
                        onClick = { isDeleteMode = !isDeleteMode })
                    .dashedBorder(
                        2.dp,
                        TertiaryOrange,
                        MaterialTheme.shapes.medium,
                        10.dp,
                        4.dp
                    )
                    .weight(1f),
                shape = MaterialTheme.shapes.medium,
                colors = cardColors(White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Remove step",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Remove step",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}