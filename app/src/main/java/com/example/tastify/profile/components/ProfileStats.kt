package com.example.tastify.profile.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastify.models.UserProfile

@Composable
fun ProfilePrivateStats(user: UserProfile) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem("RECIPES", "${user.postedRecipes}", true)
            VerticalDivider(modifier = Modifier.height(35.dp))
            StatItem("FOLLOWER", "${user.follower}", true)
            VerticalDivider(modifier = Modifier.height(35.dp))
            StatItem("FOLLOWING", "${user.followedId.size}", true)
        }
    }
}
@Composable
fun ProfilePublicStats(user: UserProfile) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 24.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem("FOLLOWER", "${user.follower}", false)
            VerticalDivider(modifier = Modifier.height(35.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            StatItem("FOLLOWING", "${user.followedId.size}", false)
            VerticalDivider(modifier = Modifier.height(35.dp), color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
            StatItem("POST", "${user.postedRecipes}",false)
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, isPrivate: Boolean) {
    val labelColor = if (isPrivate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val valueColor = if (isPrivate) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = labelColor
        )
        Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = valueColor
        )
    }
}