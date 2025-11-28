package com.biosense.app.repository

import com.biosense.app.data.dao.ChatDao
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.service.api.IGeminiApiService
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiService: IGeminiApiService
) {
    val allSessions: Flow<List<ChatSessionEntity>> = chatDao.getAllSessions()

    fun getMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun getRecentMessages(sessionId: String, limit: Int = 10): List<ChatMessageEntity> {
        return chatDao.getRecentMessages(sessionId, limit)
    }

    suspend fun generateNewSessionTitle(): String {
        val count = chatDao.getSessionCount() + 1
        return "Conversation #$count"
    }

    suspend fun createSessionWithId(id: String, title: String) {
        val newSession = ChatSessionEntity(id = id, title = title)
        chatDao.insertSession(newSession)
    }

    suspend fun sendMessage(
        sessionId: String,
        displayText: String,
        aiPrompt: String,
        isFirstMessage: Boolean
    ) {
        var finalSessionId = sessionId

        if (isFirstMessage) {
            val existing = chatDao.getSessionById(sessionId)
            if (existing == null) {
                val title = generateNewSessionTitle()
                createSessionWithId(sessionId, title)
            }
        }

        // 1) Save user-facing message
        val userMsg = ChatMessageEntity(
            sessionId = finalSessionId,
            text = displayText,
            isUser = true
        )
        chatDao.insertMessage(userMsg)

        // 2) Call AI with full prompt (context + question)
        try {
            val responseText = apiService.generateResponse(aiPrompt)

            val aiMsg = ChatMessageEntity(
                sessionId = finalSessionId,
                text = responseText,
                isUser = false
            )
            chatDao.insertMessage(aiMsg)
        } catch (e: Exception) {
            val errorMsg = ChatMessageEntity(
                sessionId = finalSessionId,
                text = "Error connecting to AI service.",
                isUser = false
            )
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
