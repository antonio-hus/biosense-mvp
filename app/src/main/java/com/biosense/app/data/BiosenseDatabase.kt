package com.biosense.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.biosense.app.data.dao.ChatDao
import com.biosense.app.data.dao.NotificationHistoryDao
import com.biosense.app.data.dao.NotificationSettingsDao
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity
import com.biosense.app.data.entity.NotificationHistoryEntity
import com.biosense.app.data.entity.NotificationSettingsEntity
import com.biosense.app.data.migration.MIGRATION_1_3
import com.biosense.app.data.migration.MIGRATION_2_3

/**
 * The central Room Database definition for the Biosense app.
 * Manages persistence for chat sessions, messages, notification settings, and notification history.
 *
 * @version 3 - Added notification history.
 */
@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        NotificationSettingsEntity::class,
        NotificationHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BiosenseDatabase : RoomDatabase() {

    /**
     * Access point for Chat Data Access Object.
     */
    abstract fun chatDao(): ChatDao

    /**
     * Access point for Notification Settings Data Access Object.
     */
    abstract fun notificationSettingsDao(): NotificationSettingsDao

    /**
     * Access point for Notification History Data Access Object.
     */
    abstract fun notificationHistoryDao(): NotificationHistoryDao

    companion object {
        @Volatile private var INSTANCE: BiosenseDatabase? = null

        /**
         * Returns the singleton instance of the database.
         * Ensures thread-safe creation to prevent multiple database connections.
         */
        fun getDatabase(context: Context): BiosenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BiosenseDatabase::class.java,
                    "biosense_db"
                )
                    .addMigrations(
                        MIGRATION_1_3, // Migrate from version 1 to 3 (for databases created at v1)
                        MIGRATION_2_3  // Migrate from version 2 to 3 (for databases created at v2)
                    )
                    .fallbackToDestructiveMigrationOnDowngrade() // Only for downgrades, not upgrades
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
