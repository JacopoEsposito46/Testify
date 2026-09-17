package com.example.tastify.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastify.models.CookingRole
import com.example.tastify.models.CuisineType
import com.example.tastify.models.DietaryRestriction
import com.example.tastify.models.ProfileFrame
import com.example.tastify.models.UserProfile

@Composable
fun EditProfileInfoSection(
    profile: UserProfile,
    nameError: String?,
    emailError: String?,
    surnameError: String?,
    usernameError: String?,
    onProfileChange: (UserProfile) -> Unit,
){
    Column {
        CustomTextField(
            label = "NAME",
            value = profile.name,
            onValueChange = { onProfileChange(profile.copy(name = it)) },
            isError = nameError != null,
            errorMessage = nameError
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            label = "SURNAME",
            value = profile.surname,
            onValueChange = { onProfileChange(profile.copy(surname = it)) },
            isError = surnameError != null,
            errorMessage = surnameError
        )
        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            label = "USERNAME",
            value = profile.username,
            onValueChange = { onProfileChange(profile.copy(username = it)) },
            isError = usernameError != null,
            errorMessage = usernameError
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            label = { Text("EMAIL")},
            value = profile.email,
            onValueChange = { onProfileChange(profile.copy(email = it)) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
fun EditProfileDietaryRestrictionsSection(profile: UserProfile, title: String, onProfileChange: (UserProfile) -> Unit){
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.RestaurantMenu,
                contentDescription = "RestaurantMenu Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        val allRestrictions = DietaryRestriction.entries
        val rows = allRestrictions.chunked(2)

        for(rowItems in rows){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for(restriction in rowItems){
                    val isChecked = profile.selectedDietaryRestriction.contains(restriction)
                    ToggleOption(
                        modifier = Modifier.weight(1f),
                        label = restriction.displayName,
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            val newRestrictions = if(checked) {
                                profile.selectedDietaryRestriction + restriction
                            } else {
                                profile.selectedDietaryRestriction - restriction
                            }
                            onProfileChange(profile.copy(selectedDietaryRestriction = newRestrictions))
                        }
                    )
                }
                if(rowItems.size == 1){
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditProfileCuisinesSection(
    profile: UserProfile,
    title: String,
    onProfileChange: (UserProfile) -> Unit
){
    Column{
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = "Favorite Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        val allCuisines = CuisineType.entries
        val rows = allCuisines.chunked(2)

        for(rowItems in rows){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ){
                for(cuisine in rowItems){
                    val isChecked = profile.favoritesCuisineType.contains(cuisine)
                    ToggleOption(
                        modifier = Modifier.weight(1f),
                        label = cuisine.displayName,
                        checked = isChecked,
                        onCheckedChange = { checked ->
                            val newCuisines = if(checked) {
                                profile.favoritesCuisineType + cuisine
                            } else {
                                profile.favoritesCuisineType - cuisine
                            }
                            onProfileChange(profile.copy(favoritesCuisineType = newCuisines))
                        }
                    )
                }
                if(rowItems.size == 1){
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileRoleSection(
    profile: UserProfile,
    availableRoles: List<CookingRole>,
    onProfileChange: (UserProfile) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Title Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Your Title", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = profile.cookingRole.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Title") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableRoles.forEach { role ->
                    DropdownMenuItem(
                        text = { Text(role.displayName) },
                        onClick = {
                            onProfileChange(profile.copy(cookingRole = role))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileFrameSection(
    profile: UserProfile,
    availableFrames: List<ProfileFrame>,
    onProfileChange: (UserProfile) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Frame Icon",
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Your Frame", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = profile.chosenFrame.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Frame") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableFrames.forEach { frame ->
                    DropdownMenuItem(
                        text = { Text(frame.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = {
                            onProfileChange(profile.copy(chosenFrame = frame))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}