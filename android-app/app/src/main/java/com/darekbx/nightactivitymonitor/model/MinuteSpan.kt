package com.darekbx.nightactivitymonitor.model

data class MinuteSpan(val from: Int, val to: Int) {

    var items: List<LogItem> = listOf()
    var index = 0

    override fun toString(): String {
        return "${(from / 60).toString().padStart(2, '0')}:00 - ${(to / 60).toString().padStart(2, '0')}:00"
    }
}
