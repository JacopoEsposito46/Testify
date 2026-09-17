package com.example.tastify.chatbot.logic

import com.example.tastify.models.ChatMessage

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val error: String? = null
)