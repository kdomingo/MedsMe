package com.domtech.medtracker.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.PendingIntent
import androidx.core.app.TaskStackBuilder
import com.domtech.medtracker.MainActivity
import com.domtech.medtracker.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class IntakeReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0) return

        val contentIntent = TaskStackBuilder.create(context).run {
            addNextIntent(Intent(context, MainActivity::class.java))
            getPendingIntent(
                reminderId.toInt(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        NotificationHelper.notify(
            context = context,
            channelId = NotificationHelper.CHANNEL_INTAKE_ID,
            notificationId = reminderId.toInt(),
            title = context.getString(R.string.notification_intake_title),
            text = context.getString(R.string.notification_intake_text),
            contentIntent = contentIntent,
        )

        // Reschedule next occurrence for this reminder.
        CoroutineScope(Dispatchers.Default).launch {
            scheduler.scheduleReminder(reminderId)
        }
    }

    companion object {
        const val ACTION_REMINDER = "com.domtech.medtracker.ACTION_INTAKE_REMINDER"
        const val EXTRA_REMINDER_ID = "reminderId"
    }
}

