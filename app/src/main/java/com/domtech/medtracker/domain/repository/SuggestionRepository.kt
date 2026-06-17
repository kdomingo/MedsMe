package com.domtech.medtracker.domain.repository

import com.domtech.medtracker.domain.models.MedicationSuggestion

interface SuggestionRepository {
    suspend fun getSuggestions(query: String): List<MedicationSuggestion>
}
