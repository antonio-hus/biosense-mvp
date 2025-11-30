package com.biosense.app.data.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from database version 1 to version 3.
 * Adds both notification_settings and notification_history tables.
 * This handles the case where the database was created at version 1 (with only chat tables).
 */
val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create notification_settings table (added in version 2)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_settings (
                id TEXT NOT NULL PRIMARY KEY,
                enabled INTEGER NOT NULL,
                startHour INTEGER NOT NULL,
                endHour INTEGER NOT NULL,
                allowOvernight INTEGER NOT NULL,
                checkIntervalMinutes INTEGER NOT NULL
            )
        """.trimIndent())
        
        // Create notification_history table (added in version 3)
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_history (
                id TEXT NOT NULL PRIMARY KEY,
                message TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                category TEXT
            )
        """.trimIndent())
        
        // Create index on timestamp for faster queries
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_notification_history_timestamp 
            ON notification_history(timestamp)
        """.trimIndent())
    }
}

