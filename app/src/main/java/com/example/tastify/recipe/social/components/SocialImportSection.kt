package com.example.tastify.recipe.social.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastify.R
import com.example.tastify.recipe.social.domain.openSocialApp

@Composable
fun SocialImportSection(context: Context) {
    Text(
        text = "Import from Social",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SocialButton(
            modifier = Modifier.weight(1f),
            painter = painterResource(id = R.drawable.ic_facebook),
            color = Color.Unspecified,
            onClick = { context.openSocialApp("fb://feed", "https://www.facebook.com") }
        )
        SocialButton(
            modifier = Modifier.weight(1f),
            painter = painterResource(id = R.drawable.ic_instagram),
            color = Color(0xFFE4405F),
            onClick = { context.openSocialApp("http://instagram.com/_u/", "https://www.instagram.com") }
        )
        SocialButton(
            modifier = Modifier.weight(1f),
            painter = painterResource(id = R.drawable.ic_tiktok),
            color = Color.Black,
            onClick = { context.openSocialApp("snssdk1128://feed", "https://www.tiktok.com") }
        )
        SocialButton(
            modifier = Modifier.weight(1f),
            painter = painterResource(id = R.drawable.ic_pinterest),
            color = Color(0xFFE60023),
            onClick = { context.openSocialApp("pinterest://", "https://www.pinterest.com") }
        )
    }
}

@Composable
fun SocialButton(modifier: Modifier, painter: Painter, color: Color, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painter,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
