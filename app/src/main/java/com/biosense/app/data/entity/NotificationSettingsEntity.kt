package com.biosense.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores user preferences for health notifications.
 * Only one settings record should exist (singleton pattern).
 *
 * @property id Always "settings" to ensure singleton.
 * @property enabled Whether notifications are enabled.
 * @property startHour Hour when notifications can start (0-23).
 * @property endHour Hour when notifications should stop (0-23).
 * @property allowOvernight Whether notifications can be sent during night hours (default false).
 * @property checkIntervalMinutes How often to check health data (default 60 minutes).
 */
@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val id: String = "settings",
    val enabled: Boolean = true,
    val startHour: Int = 8, // Default: 8 AM
    val endHour: Int = 22, // Default: 10 PM
    val allowOvernight: Boolean = false,
    val checkIntervalMinutes: Int = 60
)


