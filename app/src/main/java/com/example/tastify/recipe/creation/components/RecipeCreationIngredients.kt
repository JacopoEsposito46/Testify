package com.example.tastify.recipe.creation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.tastify.models.Recipe
import com.example.tastify.models.RecipeIngredient
import com.example.tastify.models.Ingredient
import com.example.tastify.recipe.dashedBorder
import com.example.tastify.ui.theme.TertiaryOrange
import com.example.tastify.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCreationIngredients(
    recipe: Recipe,
    ingredientCatalog: List<Ingredient> = emptyList(),
    onValueChange: (Recipe) -> Unit = {},
    ingredientsError: String? = null,
    ingredientNameErrors: Map<Int, String> = emptyMap(),
    ingredientQuantityErrors: Map<Int, String> = emptyMap(),
    ingredientUnitErrors: Map<Int, String> = emptyMap(),
    onClearIngredientsError: () -> Unit = {},
    onClearIngredientNameError: (Int) -> Unit = {},
    onClearIngredientQuantityError: (Int) -> Unit = {},
    onClearIngredientUnitError: (Int) -> Unit = {}
){
    var isDeleteMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Ingredients",
            style = MaterialTheme.typography.titleMedium
        )

        if (ingredientsError != null) {
            Text(
                text = ingredientsError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                val nameError = ingredientNameErrors[index]
                val quantityError = ingredientQuantityErrors[index]
                val unitError = ingredientUnitErrors[index]

                var quantityText by remember(ingredient.id + "_qty_$index") {
                    mutableStateOf(if (ingredient.quantity == 0.0) "" else ingredient.quantity.toString().removeSuffix(".0"))
                }

                var expanded by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isDeleteMode) {
                        IconButton(
                            onClick = {
                                val newList = recipe.ingredients.toMutableList()
                                if (newList.size > 1) {
                                    newList.removeAt(index)
                                    onValueChange(recipe.copy(ingredients = newList))
                                } else {
                                    newList.removeAt(index)
                                    onValueChange(recipe.copy(ingredients = newList))
                                }
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Remove ingredient",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    // Name AutoComplete Field
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = ingredient.ingredient.name,
                            onValueChange = { newValue ->
                                val newList = recipe.ingredients.toMutableList()
                                newList[index] = newList[index].copy(ingredient = Ingredient(ingredientId = "", name = newValue))
                                onValueChange(recipe.copy(ingredients = newList))
                                onClearIngredientNameError(index)
                                onClearIngredientsError()
                                expanded = true
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            isError = nameError != null,
                            supportingText = {
                                if (nameError != null) {
                                    Text(text = nameError, color = MaterialTheme.colorScheme.error)
                                }
                            },
                            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable, true),
                            trailingIcon = {
                                if (ingredient.ingredient.name.isNotEmpty()) {
                                    IconButton(onClick = {
                                        val newList = recipe.ingredients.toMutableList()
                                        newList[index] = newList[index].copy(ingredient = Ingredient())
                                        onValueChange(recipe.copy(ingredients = newList))
                                        expanded = false
                                    }){
                                        Icon(imageVector = Icons.Filled.Clear, contentDescription = "Clear")
                                    }
                                } else {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                }
                            },
                            label = { Text(text = "Name", style = MaterialTheme.typography.titleMedium) },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )

                        val filteredOptions = ingredientCatalog.filter {
                            it.name.contains(ingredient.ingredient.name, ignoreCase = true)
                        }

                        if (filteredOptions.isNotEmpty() && ingredient.ingredient.name.isNotEmpty()) {
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                filteredOptions.forEach { catalogIngredient ->
                                    DropdownMenuItem(
                                        text = { Text(catalogIngredient.name) },
                                        onClick = {
                                            val newList = recipe.ingredients.toMutableList()
                                            newList[index] = newList[index].copy(ingredient = catalogIngredient)
                                            onValueChange(recipe.copy(ingredients = newList))
                                            expanded = false
                                            onClearIngredientNameError(index)
                                            onClearIngredientsError()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Quantity Input Field
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newValue ->
                            val sanitized = newValue.replace(',', '.')
                            if (sanitized.isEmpty() || sanitized.matches(Regex("^\\d*\\.?\\d*$"))) {
                                quantityText = sanitized
                                val quantityDouble = sanitized.toDoubleOrNull() ?: 0.0
                                val newList = recipe.ingredients.toMutableList()
                                newList[index] = newList[index].copy(quantity = quantityDouble)
                                onValueChange(recipe.copy(ingredients = newList))
                                onClearIngredientQuantityError(index)
                                onClearIngredientsError()
                            }
                        },
                        isError = quantityError != null,
                        supportingText = {
                            if (quantityError != null) {
                                Text(
                                    text = quantityError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .width(100.dp),
                        label = {
                            Text(
                                text = "Quantity",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    //Unity
                    OutlinedTextField(
                        value = ingredient.unit,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isLetter() }) {
                                val newList = recipe.ingredients.toMutableList()
                                newList[index] = newList[index].copy(unit = newValue)
                                onValueChange(recipe.copy(ingredients = newList))
                                onClearIngredientUnitError(index)
                                onClearIngredientsError()
                            }
                        },
                        isError = unitError != null,
                        supportingText = {
                            if (unitError != null) {
                                Text(
                                    text = unitError,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .width(70.dp),
                        label = {
                            Text(
                                text = "Unit",
                                style = MaterialTheme.typography.titleMedium
                            )
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //Add ingredient button
            Card(
                modifier = Modifier
                    .clickable(onClick = {
                        val newList = recipe.ingredients + RecipeIngredient()
                        onValueChange(recipe.copy(ingredients = newList))
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
                        contentDescription = "Add ingredient",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Add ingredient",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Card(
                modifier = Modifier
                    .clickable(
                        enabled = recipe.ingredients.size > 1,
                        onClick = { isDeleteMode = !isDeleteMode }
                    )
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
                        contentDescription = "Remove ingredient",
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Remove ingredient",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}