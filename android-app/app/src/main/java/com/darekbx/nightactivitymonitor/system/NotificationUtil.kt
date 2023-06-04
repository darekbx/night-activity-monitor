package com.darekbx.nightactivitymonitor.system

import android.app.*
import android.content.Context
import androidx.core.app.NotificationCompat
import com.darekbx.nightactivitymonitor.R

class NotificationUtil(
    private val context: Context,
    private val notificationManager: NotificationManager
) {

    companion object {
        const val NOTIFICATION_ID = 210
        const val NOTIFICATION_CHANNEL_ID = "sensor_channel_id"
    }

    fun updateNotification(value: String) {
        val notification = createNotification(
            "Current readings",
            value
        )
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun createNotification(title: String, text: String): Notification {

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_night)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        var channel =
            notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)
        if (channel == null) {
            channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                title,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return builder.build()
    }

}