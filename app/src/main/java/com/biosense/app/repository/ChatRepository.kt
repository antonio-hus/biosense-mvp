package com.biosense.app.repository

import com.biosense.app.data.dao.ChatDao
import com.biosense.app.service.api.GeminiApiService
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: GeminiApiService
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun createSession(title: String = "New Chat"): String {
        val newSession = ChatSessionEntity(title = title)
        chatDao.insertSession(newSession)
        return newSession.id
    }

    suspend fun sendMessage(sessionId: String, text: String) {
        // Save User Message Locally
        val userMsg = ChatMessageEntity(sessionId = sessionId, text = text, isUser = true)
        chatDao.insertMessage(userMsg)

        // Call AI Service (Network)
        try {
            val responseText = apiService.generateResponse(text)

            // Save AI Response Locally
            val aiMsg = ChatMessageEntity(sessionId = sessionId, text = responseText, isUser = false)
            chatDao.insertMessage(aiMsg)
        } catch (e: Exception) {
            // Handle error (store error message or retry logic)
            val errorMsg = ChatMessageEntity(sessionId = sessionId, text = "Error connecting to AI service.", isUser = false)
            chatDao.insertMessage(errorMsg)
        }
    }
}
