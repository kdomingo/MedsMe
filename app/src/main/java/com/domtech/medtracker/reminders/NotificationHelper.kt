package com.domtech.medtracker.reminders

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
object NotificationHelper {
    const val CHANNEL_INTAKE_ID = "intake_reminders"
    const val CHANNEL_STOCK_ID = "low_stock"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intake = NotificationChannel(
            CHANNEL_INTAKE_ID,
            "Medication reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Reminders to take medications"
        }

        val stock = NotificationChannel(
            CHANNEL_STOCK_ID,
            "Low stock alerts",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Alerts when medication inventory is low"
        }

        nm.createNotificationChannel(intake)
        nm.createNotificationChannel(stock)
    }

    fun notify(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        text: String,
        contentIntent: PendingIntent? = null,
    ) {
        ensureChannels(context)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
        if (contentIntent != null) {
            builder.setContentIntent(contentIntent)
        }
        val n = builder.build()

        NotificationManagerCompat.from(context).notify(notificationId, n)
    }
}

