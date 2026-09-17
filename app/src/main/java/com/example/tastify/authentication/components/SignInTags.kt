package com.example.tastify.authentication.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.models.CookingRole
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.profile.components.ToggleOption

@Composable
fun SignInTags(
    selectedCuisine: List<CuisineType>,
    selectedDietary: List<DietaryRestriction>,
    selectedRole: CookingRole,
    onCuisineToggle: (CuisineType) -> Unit,
    onDietaryToggle: (DietaryRestriction) -> Unit,
    onRoleToggle: (CookingRole) -> Unit
){
    val allCuisines = CuisineType.entries
    var isCuisineExpanded by remember { mutableStateOf(false) }
    val sortedCuisines = remember(selectedCuisine, allCuisines) {
        allCuisines.sortedByDescending { selectedCuisine.contains(it) }
    }
    val visibleCuisinesTags = if (isCuisineExpanded) sortedCuisines else sortedCuisines.take(4)

    val allDietary = DietaryRestriction.entries
    var isDietaryExpanded by remember { mutableStateOf(false) }
    val sortedDietary = remember(selectedDietary, allDietary) {
        allDietary.sortedByDescending { selectedDietary.contains(it) }
    }
    val visibleDietaryTags = if (isDietaryExpanded) sortedDietary else sortedDietary.take(4)

    val allRoles = CookingRole.entries
    var isRolesExpanded by remember { mutableStateOf(false) }
    val sortedRoles = remember(selectedRole, allRoles) {
        allRoles.sortedByDescending { it == selectedRole }
    }
    val visibleRolesTags = if (isRolesExpanded) sortedRoles else sortedRoles.take(4)

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ){
        Text(
            text = "Cuisine tags",
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 2
        ) {
            visibleCuisinesTags.forEach { cuisine ->
                val isChecked = selectedCuisine.contains(cuisine)
                ToggleOption(
                    modifier = Modifier
                        .weight(1f),
                    label = cuisine.displayName,
                    checked = isChecked,
                    onCheckedChange = { onCuisineToggle(cuisine) }
                )
            }
            if (visibleCuisinesTags.size % 2 != 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = if (isCuisineExpanded) "Show less" else "Expand all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { isCuisineExpanded = !isCuisineExpanded }
                .padding(8.dp)
        )

        Text(
            text = "Dietary restriction tags",
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 2
        ){
            visibleDietaryTags.forEach { dietary ->
                val isChecked = selectedDietary.contains(dietary)
                ToggleOption(
                    modifier = Modifier
                        .weight(1f),
                    label = dietary.displayName,
                    checked = isChecked,
                    onCheckedChange = { onDietaryToggle(dietary) }
                )
            }
            if (visibleDietaryTags.size % 2 != 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = if (isDietaryExpanded) "Show less" else "Expand all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { isDietaryExpanded = !isDietaryExpanded }
                .padding(8.dp)
        )

        Text(
            text = "Cooking role tags",
            style = MaterialTheme.typography.titleMedium
        )
        FlowRow(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 2
        ) {
            visibleRolesTags.forEach { role ->
                val isChecked = selectedRole == role
                ToggleOption(
                    modifier = Modifier
                        .weight(1f),
                    label = role.displayName,
                    checked = isChecked,
                    onCheckedChange = { onRoleToggle(role) }
                )
            }
            if (visibleRolesTags.size % 2 != 0) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Text(
            text = if (isRolesExpanded) "Show less" else "Expand all",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clickable { isRolesExpanded = !isRolesExpanded }
                .padding(8.dp)
        )
    }
}