package com.domtech.medtracker.ui.util

import android.content.Context
import com.domtech.medtracker.R
import com.domtech.medtracker.data.Frequency
import com.domtech.medtracker.data.MedicationEntity
import java.util.Locale

/**
 * Optimizes dosage and frequency strings with plurality support.
 */
object FormatUtils {

    fun formatDose(amount: Double, unit: String): String {
        val amountStr = if (amount % 1.0 == 0.0) amount.toInt().toString() else "%.1f".format(Locale.US, amount)
        val pluralUnit = if (amount == 1.0) unit else pluralize(unit)
        return "$amountStr $pluralUnit"
    }

    fun formatTime12h(minutesOfDay: Int): String {
        val h24 = minutesOfDay / 60
        val m = minutesOfDay % 60
        val amPm = if (h24 < 12) "AM" else "PM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return "%d:%02d %s".format(Locale.US, h12, m, amPm)
    }

    fun formatFrequency(context: Context, med: MedicationEntity): String {
        return when (med.frequency) {
            Frequency.DAILY -> {
                val interval = med.dailyInterval
                if (interval <= 1) context.getString(R.string.daily) 
                else context.getString(R.string.every_x_days_formatted, interval)
            }
            Frequency.HOURLY -> {
                val interval = med.hourlyInterval
                if (interval <= 1) context.getString(R.string.every_hour) 
                else context.getString(R.string.every_x_hours_formatted, interval)
            }
        }
    }

    private fun pluralize(unit: String): String {
        val lower = unit.lowercase(Locale.US)
        return when {
            lower in listOf("ml", "mg", "g", "mcg", "oz") -> unit
            lower.endsWith("s") -> unit
            lower.endsWith("y") -> unit.dropLast(1) + "ies"
            else -> "${unit}s"
        }
    }

    fun getNextDoseWarning(context: Context, med: MedicationEntity, nowEpochMs: Long): String? {
        if (med.lastTakenEpochMs == 0L) return null

        val intervalMs = when (med.frequency) {
            Frequency.HOURLY -> med.hourlyInterval * 3600_000L
            Frequency.DAILY -> med.dailyInterval * 86400_000L
        }

        if (intervalMs <= 0) return null

        val nextDoseAt = med.lastTakenEpochMs + intervalMs
        val remainingMs = nextDoseAt - nowEpochMs

        if (remainingMs <= 0) return null

        val remainingDays = remainingMs / 86400_000L
        val remainingHours = (remainingMs % 86400_000L) / 3600_000L
        val remainingMinutes = (remainingMs % 3600_000L) / 60_000L

        val timeParts = mutableListOf<String>()
        if (remainingDays > 0) {
            timeParts.add(if (remainingDays == 1L) context.getString(R.string.time_day) else context.getString(R.string.time_days, remainingDays))
        }
        if (remainingHours > 0) {
            timeParts.add(if (remainingHours == 1L) context.getString(R.string.time_hour) else context.getString(R.string.time_hours, remainingHours))
        }
        if (remainingMinutes > 0 || timeParts.isEmpty()) {
            timeParts.add(if (remainingMinutes <= 1L) context.getString(R.string.time_less_than_minute) else context.getString(R.string.time_minutes, remainingMinutes))
        }

        val timeStr = when (timeParts.size) {
            1 -> timeParts[0]
            2 -> "${timeParts[0]}${context.getString(R.string.time_separator_and)}${timeParts[1]}"
            else -> "${timeParts[0]}${context.getString(R.string.time_separator_comma)}${timeParts[1]}${context.getString(R.string.time_separator_comma_and)}${timeParts[2]}"
        }

        return context.getString(R.string.next_dose_warning_prefix, timeStr)
    }
}
