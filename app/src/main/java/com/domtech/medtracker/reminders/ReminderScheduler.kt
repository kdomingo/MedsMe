package com.domtech.medtracker.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.domtech.medtracker.data.MedRepository
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduler(
    private val context: Context,
    private val repo: MedRepository,
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun rescheduleAllEnabled() {
        repo.listEnabledReminders().forEach { scheduleReminder(it.id) }
    }

    suspend fun scheduleReminder(reminderId: Long) {
        val reminder = repo.listEnabledReminders().firstOrNull { it.id == reminderId } ?: return
        scheduleNextOccurrence(reminder.id, reminder.minutesOfDay, reminder.daysOfWeekMask)
    }

    fun cancelReminder(reminderId: Long) {
        val pi = pendingIntentFor(reminderId)
        alarmManager.cancel(pi)
    }

    fun scheduleNextOccurrence(reminderId: Long, minutesOfDay: Int, daysMask: Int) {
        val triggerAt = computeNextTriggerEpochMs(minutesOfDay, daysMask)
        val pi = pendingIntentFor(reminderId)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pi,
        )
    }

    private fun pendingIntentFor(reminderId: Long): PendingIntent {
        val i = Intent(context, IntakeReminderReceiver::class.java).apply {
            action = IntakeReminderReceiver.ACTION_REMINDER
            putExtra(IntakeReminderReceiver.EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun computeNextTriggerEpochMs(minutesOfDay: Int, daysMask: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        val today = LocalDate.now(zone)
        val time = LocalTime.of(minutesOfDay / 60, minutesOfDay % 60)

        fun dayBitFor(dow: DayOfWeek): Int {
            // 0=Mon ... 6=Sun
            val idx = (dow.value + 6) % 7
            return 1 shl idx
        }

        for (i in 0..7) {
            val date = today.plusDays(i.toLong())
            val candidate = LocalDateTime.of(date, time)
            val inFuture = candidate.isAfter(now)
            val allowed = (daysMask and dayBitFor(candidate.dayOfWeek)) != 0
            if (allowed && inFuture) {
                return candidate.atZone(zone).toInstant().toEpochMilli()
            }
        }

        // Fallback: tomorrow same time.
        return LocalDateTime.of(today.plusDays(1), time).atZone(zone).toInstant().toEpochMilli()
    }
}

