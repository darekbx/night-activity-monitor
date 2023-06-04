package com.darekbx.nightactivitymonitor.repository.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entry")
data class LogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
)