package com.darekbx.nightactivitymonitor.model

data class LogItem(val timestamp: Long) {

    var minutes: Int = 0
    var formattedTime: String? = null

    override fun toString(): String {
        return "Date: $formattedTime, minutes: $minutes"
    }
}