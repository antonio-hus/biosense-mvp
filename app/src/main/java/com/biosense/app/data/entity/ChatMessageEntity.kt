package com.biosense.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index
import java.util.UUID

/**
 * Represents an individual message within a chat session.
 * Linked to [ChatSessionEntity] via foreign key with cascading delete.
 *
 * @property id Unique UUID string for the message.
 * @property sessionId Foreign key referencing the parent [ChatSessionEntity].
 * @property text The content of the message (either user input or AI response).
 * @property isUser True if sent by the user, False if sent by the AI.
 * @property timestamp Time the message was created/received.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // Deleting a session deletes all its messages
        )
    ],
    indices = [Index(value = ["sessionId"])] // Index for faster lookup by session
)
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sessionId: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
