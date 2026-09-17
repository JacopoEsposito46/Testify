package com.example.tastify.chatbot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.tastify.chatbot.logic.ChatEvent
import com.example.tastify.chatbot.logic.ChatViewModel
import com.example.tastify.chatbot.components.BotTypingIndicator
import com.example.tastify.chatbot.components.ChatInputArea
import com.example.tastify.chatbot.components.ChatIntelligenceBadge
import com.example.tastify.chatbot.components.ChatMessageItem
import com.example.tastify.components.SharedBottomNavBar
import com.example.tastify.components.SharedTopBar

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    unreadNotificationCount: Int = 0,
    onNavigateBack: () -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCookBook: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChat: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(state.messages.size, state.isTyping) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size)
        }
    }

    Scaffold(
        topBar = {
            SharedTopBar(
                title = "Chef Assistant",
                onBack = onNavigateBack
            )
        },
        bottomBar = {
            SharedBottomNavBar(
                selectedRoute = "chat",
                onRouteSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "cookbook" -> onNavigateToCookBook()
                        "updates" -> onNavigateToNotifications()
                        "profile" -> onNavigateToProfile()
                        "chat" -> onNavigateToChat()
                    }
                },
                unreadNotificationCount = unreadNotificationCount
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    ChatIntelligenceBadge()
                }

                items(state.messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onRecipeClick = onNavigateToRecipe
                    )
                }

                if (state.isTyping) {
                    item {
                        BotTypingIndicator()
                    }
                }
            }

            ChatInputArea(
                inputText = inputText,
                isSending = state.isTyping,
                onInputChanged = { inputText = it },
                onSend = {
                    if (!state.isTyping) {
                        viewModel.onEvent(ChatEvent.SendMessage(inputText))
                        inputText = ""
                    }
                }
            )
        }
    }
}
