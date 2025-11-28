package com.biosense.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single conversation thread with the AI Advisor.
 * Acts as the parent entity for [ChatMessageEntity].
 *
 * @property id Unique UUID string for the session.
 * @property title Display title of the conversation (e.g., "Conversation #1").
 * @property createdAt Timestamp of creation, used for sorting history.
 */
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis()
)
