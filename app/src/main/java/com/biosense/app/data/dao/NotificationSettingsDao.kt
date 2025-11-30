package com.biosense.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.biosense.app.data.entity.NotificationSettingsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for notification settings.
 * Manages the singleton notification settings record.
 */
@Dao
interface NotificationSettingsDao {
    
    /**
     * Observes the notification settings in real-time.
     */
    @Query("SELECT * FROM notification_settings WHERE id = 'settings'")
    fun getSettings(): Flow<NotificationSettingsEntity?>
    
    /**
     * Gets the settings synchronously.
     */
    @Query("SELECT * FROM notification_settings WHERE id = 'settings'")
    suspend fun getSettingsSync(): NotificationSettingsEntity?
    
    /**
     * Inserts or updates the settings.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: NotificationSettingsEntity)
    
    /**
     * Updates the settings.
     */
    @Update
    suspend fun update(settings: NotificationSettingsEntity)
}


