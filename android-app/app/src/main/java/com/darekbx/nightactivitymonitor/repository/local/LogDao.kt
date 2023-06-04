package com.darekbx.nightactivitymonitor.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    @Query("SELECT timestamp FROM log_entry ORDER BY timestamp ASC")
    fun fetch(): Flow<List<Long>>

    @Query("DELETE FROM log_entry")
    suspend fun deleteAll()

    @Insert
    suspend fun add(logEntry: LogEntry)
}
