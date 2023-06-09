package com.darekbx.nightactivitymonitor.repository

import com.darekbx.nightactivitymonitor.model.LogItem
import com.darekbx.nightactivitymonitor.model.MinuteSpan
import com.darekbx.nightactivitymonitor.repository.local.LogDao
import com.darekbx.nightactivitymonitor.repository.local.LogEntry
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LocalRepository(private val logDao: LogDao) {

    private var spans: List<MinuteSpan>


    init {
        var i = 0
        spans =
            (19..23).map { h -> MinuteSpan(h * 60, (h + 1) * 60).apply { this.index = i++ } } +
                    (0..9).map { h -> MinuteSpan(h * 60, (h + 1) * 60).apply { this.index = i++ } }
    }

    suspend fun notifyDetectedMovement() {
        logDao.add(LogEntry(null, System.currentTimeMillis()))
    }

    fun logFlow() = logDao
        .fetch()
        .map { list ->
            spans.forEach { span ->
                span.items = list
                    .map { timestamp ->
                        LogItem(timestamp).apply {
                            minutes = timestamp.toMinutes()
                            formattedTime = dateTimeFormat.format(Date(timestamp))
                        }
                    }
                    .filter { item -> item.minutes >= span.from && item.minutes < span.to }
            }
            return@map spans.sortedBy { it.index }
        }

    suspend fun deleteAll() {
        logDao.deleteAll()
    }

    private fun Long.toMinutes(): Int {
        val date = Calendar.getInstance()
        date.timeInMillis = this
        val hours = date.get(Calendar.HOUR_OF_DAY)
        val minutes = date.get(Calendar.MINUTE)
        return hours * 60 + minutes
    }

    private val dateTimeFormat by lazy {
        SimpleDateFormat("yyyy-mm-dd HH:mm", Locale.getDefault())
    }
}
