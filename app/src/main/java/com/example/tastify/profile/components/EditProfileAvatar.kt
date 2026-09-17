package com.example.tastify.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tastify.models.UserProfile

@Composable
fun EditProfileAvatarSection(
    profile: UserProfile,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    sizeFraction: Float = 0.4f
){
    var expanded by remember { mutableStateOf(false) }

    Box(contentAlignment = Alignment.Center){

        ProfileImage(profile, sizeFraction)

        // edit badge
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ){
            Icon(Icons.Default.CameraAlt, contentDescription = "Edit Photo", tint = MaterialTheme.colorScheme.surface)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            DropdownMenuItem(
                text = { Text("Take a photo") },
                onClick = {
                    expanded = false
                    onCameraClick()
                }
            )
            DropdownMenuItem(
                text = { Text("Choose from gallery") },
                onClick = {
                    expanded = false
                    onGalleryClick()
                }
            )
        }
    }
}