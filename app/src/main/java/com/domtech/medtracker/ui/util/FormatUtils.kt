package com.domtech.medtracker.ui.util

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

    fun formatFrequency(med: MedicationEntity): String {
        return when (med.frequency) {
            Frequency.DAILY -> "Daily"
            Frequency.HOURLY -> {
                val interval = med.hourlyInterval
                if (interval <= 1) "Every hour" else "Every $interval hours"
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

    fun getNextDoseWarning(med: MedicationEntity, nowEpochMs: Long): String? {
        if (med.frequency != Frequency.HOURLY || med.lastTakenEpochMs == 0L) return null

        val intervalMs = med.hourlyInterval * 3600_000L
        val nextDoseAt = med.lastTakenEpochMs + intervalMs
        val remainingMs = nextDoseAt - nowEpochMs

        if (remainingMs <= 0) return null

        val remainingHours = remainingMs / 3600_000L
        val remainingMinutes = (remainingMs % 3600_000L) / 60_000L

        val timeStr = when {
            remainingHours > 0 -> {
                val hStr = if (remainingHours == 1L) "1 hour" else "$remainingHours hours"
                val mStr = if (remainingMinutes > 0) " and ${if (remainingMinutes == 1L) "1 minute" else "$remainingMinutes minutes"}" else ""
                "$hStr$mStr"
            }
            else -> if (remainingMinutes <= 1) "less than a minute" else "$remainingMinutes minutes"
        }

        return "Warning: Your next dose is not due for another $timeStr. Taking it now may exceed your recommended dosage."
    }
}
