package com.domtech.medtracker.reminders

import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.app.TaskStackBuilder
import com.domtech.medtracker.MainActivity
import com.domtech.medtracker.MedTrackerApplication

class LowStockWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as MedTrackerApplication
        val low = app.repo.listLowStock()
        if (low.isEmpty()) return Result.success()

        val title = "Low medication stock"
        val text = low.take(3).joinToString { "${it.name}: ${it.currentLevel}" } +
            if (low.size > 3) " (+${low.size - 3} more)" else ""

        val contentIntent = TaskStackBuilder.create(applicationContext).run {
            addNextIntent(Intent(applicationContext, MainActivity::class.java))
            getPendingIntent(
                10_000,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        NotificationHelper.notify(
            context = applicationContext,
            channelId = NotificationHelper.CHANNEL_STOCK_ID,
            notificationId = 10_000,
            title = title,
            text = text,
            contentIntent = contentIntent,
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "low_stock_check"
    }
}

