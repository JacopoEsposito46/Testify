package com.example.tastify.data.local

import com.example.tastify.data.ChatLocalRepository
import com.example.tastify.models.ChatMessage
import io.paperdb.Paper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatLocalRepositoryImpl @Inject constructor() : ChatLocalRepository {

    private val bookName = "chat_book"
    private val messagesKey = "messages_key"

    init {
        Paper.book(bookName).destroy()
    }

    override suspend fun getMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        Paper.book(bookName).read<List<ChatMessage>>(messagesKey, emptyList()) ?: emptyList()
    }

    override suspend fun saveMessages(messages: List<ChatMessage>) {
        withContext(Dispatchers.IO) {
            Paper.book(bookName).write(messagesKey, messages)
        }
    }

    override fun clearMessages() {
        Paper.book(bookName).destroy()
    }
}