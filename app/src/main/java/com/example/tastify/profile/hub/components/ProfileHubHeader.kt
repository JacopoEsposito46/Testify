package com.example.tastify.profile.hub.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastify.models.UserProfile
import com.example.tastify.profile.components.ProfileImage

@Composable
fun ProfileHubHeader(
    userProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        userProfile?.let { user ->
            ProfileImage(user, 0.6f)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = userProfile?.username?.let { "@$it" } ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}