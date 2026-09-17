package com.example.tastify.data

import com.example.tastify.models.ChatMessage

interface ChatLocalRepository {
    suspend fun getMessages(): List<ChatMessage>
    suspend fun saveMessages(messages: List<ChatMessage>)
    fun clearMessages()
}