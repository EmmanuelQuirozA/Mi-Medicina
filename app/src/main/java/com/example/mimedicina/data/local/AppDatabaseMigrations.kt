package com.example.mimedicina.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS reminder_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    medicine_id INTEGER NOT NULL,
                    profile_id INTEGER NOT NULL,
                    medicine_name TEXT NOT NULL,
                    scheduled_time_millis INTEGER NOT NULL,
                    action_time_millis INTEGER,
                    action_type TEXT,
                    FOREIGN KEY(medicine_id) REFERENCES medicines(id) ON DELETE CASCADE,
                    FOREIGN KEY(profile_id) REFERENCES profiles(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reminder_history_profile_id ON reminder_history(profile_id)"
            )
            database.execSQL(
                "CREATE INDEX IF NOT EXISTS index_reminder_history_medicine_id ON reminder_history(medicine_id)"
            )
        }
    }
}
