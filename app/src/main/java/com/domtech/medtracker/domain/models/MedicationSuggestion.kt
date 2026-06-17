package com.domtech.medtracker.domain.models

import com.domtech.medtracker.data.Frequency

data class MedicationSuggestion(
    val name: String,
    val genericName: String? = null,
    val doseAmount: Double? = null,
    val doseUnit: String = "tablet",
    val frequencyHint: Frequency? = null,
    val dailyIntervalHint: Int? = null,
    val hourlyIntervalHint: Int? = null,
    val usageNote: String? = null,
    val warning: String? = null
)
