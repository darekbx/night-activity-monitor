package com.darekbx.nightactivitymonitor.model

data class MinuteSpan(val from: Int, val to: Int) {

    var items: List<LogItem> = listOf()

    override fun toString(): String {
        val fromHours = from / 60
        val fromMins = from - (fromHours * 60)
        val toHours = to / 60
        val toMins = to - (toHours * 60)
        return "%02d:%02d - %02d:%02d".format(fromHours, fromMins, toHours, toMins)
    }
}
