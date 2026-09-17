package com.example.tastify.authentication.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.tastify.models.UserProfile
import com.example.tastify.profile.components.ProfileImage
import com.example.tastify.models.ImageSource

@Composable
fun SignInForm(
    name: String,
    surname: String,
    email: String,
    username: String,
    usernameError: String?,
    photoUrl: String,
    onUsernameChange: (String) -> Unit,
) {
    val temporaryUser = UserProfile(
        name = name,
        surname = surname,
        email = email,
        username = username,
        profilePicture = if (photoUrl.isNotEmpty()) ImageSource.Uri(photoUrl) else null
    )
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ProfileImage(
            user = temporaryUser,
            sizeFraction = 0.3f
        )
        OutlinedTextField(
            value = name,
            onValueChange = {},
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            label = {
                Text(
                    text = "Name",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = surname,
            onValueChange = {},
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            label = {
                Text(
                    text = "Surname",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = {},
            readOnly = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            label = {
                Text(
                    text = "Email",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
        OutlinedTextField(
            value = username,
            onValueChange = { onUsernameChange(it) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences
            ),
            isError = usernameError != null,
            supportingText = {
                if (usernameError != null) {
                    Text(
                        text = usernameError,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            trailingIcon = {
                if (username.isNotEmpty()) {
                    IconButton(onClick = {
                        onUsernameChange("")
                    }){
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Clear"
                        )
                    }
                }
            },
            label = {
                Text(
                    text = "Nickname",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}