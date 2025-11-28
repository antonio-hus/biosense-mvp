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

    suspend fun getRecentMessages(sessionId: String, limit: Int): List<ChatMessageEntity> {
        return chatDao.getRecentMessages(sessionId, limit)
    }

    suspend fun generateNewSessionTitle(): String {
        val count = chatDao.getSessionCount() + 1
        return "Conversation #$count"
    }

    // CRITICAL: This function now explicitly uses the ID passed to it
    suspend fun createSessionWithId(id: String, title: String) {
        val newSession = ChatSessionEntity(id = id, title = title)
        chatDao.insertSession(newSession)
    }

    /**
     * Sends message to AI and saves result to DB.
     * Returns the context update string if the AI requested one (e.g. "User has flu"), or null.
     */
    suspend fun sendMessage(
        sessionId: String,
        displayText: String,
        aiPrompt: String,
        isFirstMessage: Boolean
    ): String? {

        // 1. Ensure Session Exists (Using the exact sessionId passed from ViewModel)
        if (isFirstMessage) {
            val existing = chatDao.getSessionById(sessionId)
            if (existing == null) {
                val title = generateNewSessionTitle()
                createSessionWithId(sessionId, title)
            }
        }

        // 2. Insert User Message immediately (UI will see this via Flow)
        val userMsg = ChatMessageEntity(
            sessionId = sessionId,
            text = displayText,
            isUser = true
        )
        chatDao.insertMessage(userMsg)

        // 3. Call AI Service
        try {
            val fullResponse = apiService.generateResponse(aiPrompt)

            // 4. Parse SET_CONTEXT command
            val updateRegex = "\\[SET_CONTEXT:(.*?)\\]".toRegex()
            val match = updateRegex.find(fullResponse)

            // If found, this is the NEW complete state
            val newContextState = match?.groupValues?.get(1)?.trim()

            // Remove tag from display text
            val displayResponse = fullResponse.replace(updateRegex, "").trim()

            // 5. Insert AI Message
            val aiMsg = ChatMessageEntity(
                sessionId = sessionId,
                text = displayResponse,
                isUser = false
            )
            chatDao.insertMessage(aiMsg)

            return newContextState

        } catch (e: Exception) {
            // On error, insert error message
            val errorMsg = ChatMessageEntity(
                sessionId = sessionId,
                text = "I'm having trouble connecting right now. Please try again.",
                isUser = false
            )
            chatDao.insertMessage(errorMsg)
            return null
        }
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
    }

    suspend fun renameSession(sessionId: String, newTitle: String) {
        chatDao.updateSessionTitle(sessionId, newTitle)
    }
}
