package com.example.tastify.profile.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tastify.models.ImageSource
import com.example.tastify.models.UserProfile
import com.example.tastify.models.ProfileFrame
import com.example.tastify.utils.LevelSystem
import com.example.tastify.ui.theme.*
import com.example.tastify.utils.withCacheBuster

@Composable
fun getFrameColor(frame: ProfileFrame): Color {
    return when (frame) {
        ProfileFrame.BRONZE -> Bronze
        ProfileFrame.SILVER -> Silver
        ProfileFrame.GOLD -> Gold
        ProfileFrame.PLATINUM -> Platinum
        ProfileFrame.DIAMOND -> Diamond
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProfileImage(user: UserProfile, sizeFraction: Float) {
    val progress = LevelSystem.getLevelProgress(user.experiencePoints)
    val maxFrame = LevelSystem.getFrameForLevel(LevelSystem.calculateLevel(user.experiencePoints))
    val visibleFrame = user.chosenFrame.takeIf { it.ordinal <= maxFrame.ordinal } ?: maxFrame
    val frameColor = getFrameColor(visibleFrame)

    BoxWithConstraints {
        val dimension = if (maxWidth < maxHeight) maxWidth * sizeFraction else maxHeight * sizeFraction

        Box(
            modifier = Modifier.size(dimension),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = frameColor,
                trackColor = GrayBackground,
                strokeWidth = 4.dp
            )

            Box(
                modifier = Modifier
                    .size(dimension - 16.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false
                    )
                    .clip(CircleShape)
                    .border(3.dp, frameColor, CircleShape)
                    .background(SecondaryOrange),
                contentAlignment = Alignment.Center
            ) {
                if (user.profilePicture != null) {
                    when (val source = user.profilePicture) {
                        is ImageSource.Resource -> {
                            Image(
                                painter = painterResource(id = source.resId),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        is ImageSource.Uri -> {
                            AsyncImage(
                                model = source.uriString.withCacheBuster(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {}
                    }
                } else {
                    val initials = remember(user.name, user.surname) {
                        val n = user.name.take(1).uppercase()
                        val s = user.surname.take(1).uppercase()
                        if (n.isEmpty() && s.isEmpty()) "?" else "$n$s"
                    }
                    Text(
                        text = initials,
                        color = PrimaryOrange,
                        fontSize = (dimension.value / 4).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileIdentity(user: UserProfile) {
    val level = LevelSystem.calculateLevel(user.experiencePoints)
    val actualRank = LevelSystem.getFrameForLevel(level)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${user.name} ${user.surname}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "@${user.username} • Level $level",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "${user.cookingRole.displayName} • Rank ${actualRank.name}",
            style = MaterialTheme.typography.bodyMedium,
            color = getFrameColor(actualRank),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
