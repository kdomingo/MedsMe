package com.example.medtracker.ui.util

object Days {
    data class Day(val bit: Int, val shortLabel: String, val fullLabel: String)

    // 0=Mon ... 6=Sun
    val all: List<Day> = listOf(
        Day(1 shl 0, "Mon", "Monday"),
        Day(1 shl 1, "Tue", "Tuesday"),
        Day(1 shl 2, "Wed", "Wednesday"),
        Day(1 shl 3, "Thu", "Thursday"),
        Day(1 shl 4, "Fri", "Friday"),
        Day(1 shl 5, "Sat", "Saturday"),
        Day(1 shl 6, "Sun", "Sunday"),
    )

    fun weekdaysMask(): Int = (1 shl 0) or (1 shl 1) or (1 shl 2) or (1 shl 3) or (1 shl 4)

    fun toggle(mask: Int, bit: Int): Int = mask xor bit

    fun formatMask(mask: Int): String {
        val selected = all.filter { (mask and it.bit) != 0 }.map { it.shortLabel }
        return if (selected.isEmpty()) "None" else selected.joinToString(", ")
    }
}

fun formatMinutesOfDay(minutes: Int): String {
    val clamped = minutes.coerceIn(0, 23 * 60 + 59)
    val h = (clamped / 60).coerceIn(0, 23)
    val m = (clamped % 60).coerceIn(0, 59)
    return "%02d:%02d".format(h, m)
}

