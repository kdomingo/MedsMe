package com.domtech.medtracker.reminders

import android.content.Context
import android.app.PendingIntent
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.core.app.TaskStackBuilder
import androidx.hilt.work.HiltWorker
import com.domtech.medtracker.MainActivity
import com.domtech.medtracker.R
import com.domtech.medtracker.data.MedRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class LowStockWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: MedRepository,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val low = repo.listLowStock()
        if (low.isEmpty()) return Result.success()

        val title = applicationContext.getString(R.string.notification_low_stock_title)
        val text = low.take(3).joinToString { "${it.name}: ${it.currentLevel}" } +
            if (low.size > 3) applicationContext.getString(R.string.notification_low_stock_more, low.size - 3) else ""

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

