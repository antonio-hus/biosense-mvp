package com.biosense.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.biosense.app.data.dao.ChatDao
import com.biosense.app.data.entity.ChatMessageEntity
import com.biosense.app.data.entity.ChatSessionEntity

@Database(entities = [ChatSessionEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class BiosenseDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile private var INSTANCE: BiosenseDatabase? = null

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
