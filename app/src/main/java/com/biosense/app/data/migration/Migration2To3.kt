package com.biosense.app.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 2 to version 3.
 * Adds the notification_history table without affecting existing data.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create notification_history table (matches NotificationHistoryEntity structure)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_history (
                id TEXT NOT NULL PRIMARY KEY,
                message TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                category TEXT
            )
        """.trimIndent())
        
        // Create index on timestamp for faster queries (matches entity index)
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_notification_history_timestamp 
            ON notification_history(timestamp)
        """.trimIndent())
    }
}

