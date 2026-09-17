package com.example.tastify.recipe.details.components

import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.models.Recipe

@Composable
fun RecipeStats(recipe: Recipe, currentServes: Int, isOwner: Boolean, onIncrease: () -> Unit, onDecrease: () -> Unit){

    val df = DecimalFormat("#.##")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            //Calories
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalFireDepartment,
                        contentDescription = "calories",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${df.format((recipe.calories / recipe.serves) * currentServes)} kcal",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.secondary,
                thickness = 1.dp
            )

            //Cost
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Paid,
                        contentDescription = "cost",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Cost",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..recipe.cost).forEach { _ ->
                            Icon(
                                imageVector = Icons.Filled.AttachMoney,
                                contentDescription = "cost",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .size(25.dp)
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            color = MaterialTheme.colorScheme.secondary,
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            //CookTime
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp, end = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = "cookTime",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${recipe.cookTime} min",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.secondary,
                thickness = 1.dp
            )

            //Serves
            Card(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.People,
                        contentDescription = "serves",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Serves",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        if(!isOwner) {
                            IconButton(
                                onClick = onDecrease,
                                enabled = currentServes > 2,
                                modifier = Modifier
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveCircleOutline,
                                    contentDescription = "minus",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier
                                        .size(25.dp)
                                )
                            }
                        }
                        Text(
                            text = currentServes.toString(),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        if(!isOwner) {
                            IconButton(
                                onClick = onIncrease,
                                enabled = currentServes < 10,
                                modifier = Modifier
                                    .size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircleOutline,
                                    contentDescription = "plus",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(25.dp)
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}
