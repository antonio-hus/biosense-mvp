package com.biosense.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a notification that was sent to the user.
 * Stores the notification message, timestamp, and metadata for history tracking.
 *
 * @property id Unique UUID string for the notification record.
 * @property message The notification message that was displayed.
 * @property timestamp When the notification was sent (milliseconds since epoch).
 * @property category Optional category/type of notification (e.g., "activity", "health", "hydration").
 */
@Entity(
    tableName = "notification_history",
    indices = [Index(value = ["timestamp"])] // Index for faster date-based queries
)
data class NotificationHistoryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String? = null // Optional: "activity", "health", "hydration", etc.
)

