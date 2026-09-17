package com.example.tastify.chatbot.logic

sealed interface ChatEvent {
    data class SendMessage(val text: String) : ChatEvent
    data object DismissError : ChatEvent
}