package com.biosense.app.data.repository

import com.biosense.app.data.dao.ChatDao
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.service.api.GeminiApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: GeminiApiService
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    // NEW: Generate a unique title based on existing count
    suspend fun generateNewSessionTitle(): String {
        val count = chatDao.getSessionCount() + 1
        return "Conversation #$count"
    }

    // NEW: Create session explicitly
    suspend fun createSession(title: String): String {
        val newSession = ChatSessionEntity(id = UUID.randomUUID().toString(), title = title)
        chatDao.insertSession(newSession)
        return newSession.id
    }

    // UPDATED: Handles "First Message" logic
    suspend fun sendMessage(sessionId: String, text: String, isFirstMessage: Boolean) {
        var finalSessionId = sessionId

        // If this is the very first message of a "temporary" session, create it now
        if (isFirstMessage) {
            // Double check if it exists, if not create it
            val sessionExists = chatDao.getSessionById(sessionId) != null
            if (!sessionExists) {
                val title = generateNewSessionTitle()
                val newSession = ChatSessionEntity(id = sessionId, title = title)
                chatDao.insertSession(newSession)
            }
        }

        // 1. Save User Message
        val userMsg = ChatMessageEntity(sessionId = finalSessionId, text = text, isUser = true)
        chatDao.insertMessage(userMsg)

        // 2. AI Service Call
        try {
            val responseText = apiService.generateResponse(text)
            val aiMsg = ChatMessageEntity(sessionId = finalSessionId, text = responseText, isUser = false)
            chatDao.insertMessage(aiMsg)
        } catch (e: Exception) {
            val errorMsg = ChatMessageEntity(sessionId = finalSessionId, text = "Error: ${e.localizedMessage}", isUser = false)
            chatDao.insertMessage(errorMsg)
        }
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun renameSession(sessionId: String, newTitle: String) {
        chatDao.updateSessionTitle(sessionId, newTitle)
    }
}
