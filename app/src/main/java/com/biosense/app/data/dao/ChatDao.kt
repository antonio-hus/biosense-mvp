package com.biosense.app.data.dao

import androidx.room.*
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for all Chat-related database operations.
 * Handles sessions and messages with reactive Flow support.
 */
@Dao
interface ChatDao {

    // --- Session Operations ---

    /**
     * Observes all chat sessions, ordered by creation time (newest first).
     * Used for the navigation drawer history list.
     */
    @Query("SELECT * FROM chat_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<ChatSessionEntity>>

    /**
     * Inserts or updates a chat session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity)

    /**
     * Deletes a specific session. Associated messages are deleted automatically via Cascade.
     */
    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    /**
     * Updates the display title of a session.
     */
    @Query("UPDATE chat_sessions SET title = :title WHERE id = :id")
    suspend fun updateSessionTitle(id: String, title: String)

    /**
     * Returns the total number of sessions, used for generating default titles (e.g. "Conversation #5").
     */
    @Query("SELECT COUNT(*) FROM chat_sessions")
    suspend fun getSessionCount(): Int

    /**
     * checks if a session exists by ID. Useful for lazy-creation logic.
     */
    @Query("SELECT * FROM chat_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): ChatSessionEntity?


    // --- Message Operations ---

    /**
     * Observes all messages for a specific session, ordered chronologically.
     * Used for the main chat screen.
     */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    /**
     * Inserts a new message (user or AI).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    /**
     * Retrieves the very last message in a session, often used for previews.
     */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(sessionId: String): ChatMessageEntity?

    /**
     * Retrieves the N most recent messages for a session to build AI context context.
     *
     * Implementation Note: Inner query fetches newest N messages (DESC),
     * outer query re-orders them chronologically (ASC) for natural reading flow.
     */
    @Query("SELECT * FROM (SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit) ORDER BY timestamp ASC")
    suspend fun getRecentMessages(sessionId: String, limit: Int): List<ChatMessageEntity>
}
