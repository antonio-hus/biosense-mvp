package com.biosense.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.biosense.app.data.dao.ChatDao
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity

/**
 * The central Room Database definition for the Biosense app.
 * Manages persistence for chat sessions and messages.
 *
 * @version 1 - Initial schema.
 */
@Database(entities = [ChatSessionEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class BiosenseDatabase : RoomDatabase() {

    /**
     * Access point for Chat Data Access Object.
     */
    abstract fun chatDao(): ChatDao

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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
