package com.example.tastify.chatbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastify.models.ChatMessage
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onRecipeClick: (String) -> Unit
) {
    val isUser = message.role == "user"
    val timeString = remember { LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = "Bot Avatar",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (isUser) 20.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 20.dp
                )
            ) {
                if (isUser) {
                    Text(
                        text = message.text,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                } else {
                    BotMessageContent(
                        text = message.text,
                        onRecipeClick = onRecipeClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (isUser) "SENT • $timeString" else "CHEF BOT • $timeString",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BotMessageContent(
    text: String,
    onRecipeClick: (String) -> Unit
) {
    val regex = Regex("\\[(.*?)\\]\\(recipe://([^|)]+)(?:\\|([^)]*))?\\)")
    var lastIndex = 0

    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        regex.findAll(text).forEach { matchResult ->
            val textBefore = text.substring(lastIndex, matchResult.range.first)
            if (textBefore.isNotBlank()) {
                Text(
                    text = textBefore.trim(),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            val recipeName = matchResult.groupValues[1]
            val recipeId = matchResult.groupValues[2]
            val imageUrl = if (matchResult.groupValues.size > 3) matchResult.groupValues[3] else ""

            RecipeLinkCard(
                name = recipeName,
                imageUrl = imageUrl,
                onClick = { onRecipeClick(recipeId) }
            )

            lastIndex = matchResult.range.last + 1
        }

        val textAfter = text.substring(lastIndex)
        if (textAfter.isNotBlank()) {
            Text(
                text = textAfter.trim(),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}