package com.biosense.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biosense.app.data.entity.NotificationHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notification history.
 * Manages querying and inserting notification records.
 */
@Dao
interface NotificationHistoryDao {
    
    /**
     * Observes all notifications, ordered by timestamp (newest first).
     */
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationHistoryEntity>>
    
    /**
     * Gets all notifications synchronously, ordered by timestamp (newest first).
     */
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC")
    suspend fun getAllNotificationsSync(): List<NotificationHistoryEntity>
    
    /**
     * Gets notifications within a date range.
     */
    @Query("SELECT * FROM notification_history WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getNotificationsInRange(startTime: Long, endTime: Long): List<NotificationHistoryEntity>
    
    /**
     * Gets notifications by category.
     */
    @Query("SELECT * FROM notification_history WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getNotificationsByCategory(category: String): List<NotificationHistoryEntity>
    
    /**
     * Gets the most recent N notifications.
     */
    @Query("SELECT * FROM notification_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentNotifications(limit: Int): List<NotificationHistoryEntity>
    
    /**
     * Inserts a new notification record.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationHistoryEntity)
    
    /**
     * Deletes a specific notification.
     */
    @Query("DELETE FROM notification_history WHERE id = :id")
    suspend fun deleteNotification(id: String)
    
    /**
     * Deletes all notifications older than the specified timestamp.
     */
    @Query("DELETE FROM notification_history WHERE timestamp < :beforeTimestamp")
    suspend fun deleteOldNotifications(beforeTimestamp: Long)
    
    /**
     * Gets the total count of notifications.
     */
    @Query("SELECT COUNT(*) FROM notification_history")
    suspend fun getNotificationCount(): Int
}

