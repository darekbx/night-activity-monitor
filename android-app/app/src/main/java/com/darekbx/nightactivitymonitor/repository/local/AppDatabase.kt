package com.darekbx.nightactivitymonitor.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LogEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun logDao() : LogDao

    companion object {
        const val DB_NAME = "local_database"
    }
}
