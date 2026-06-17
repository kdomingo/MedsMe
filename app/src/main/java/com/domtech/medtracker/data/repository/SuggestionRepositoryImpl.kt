package com.domtech.medtracker.data.repository

import com.domtech.medtracker.data.Frequency
import com.domtech.medtracker.domain.models.MedicationSuggestion
import com.domtech.medtracker.domain.repository.SuggestionRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SuggestionRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : SuggestionRepository {

    override suspend fun getSuggestions(query: String): List<MedicationSuggestion> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        return try {
            // NIH Clinical Tables API - RxTerms
            // Format: [count, [names], null, [[strengths, generic_name], ...]]
            val response: JsonArray = client.get("https://clinicaltables.nlm.nih.gov/api/rxterms/v3/search") {
                parameter("terms", trimmed)
                parameter("ef", "STRENGTHS_AND_FORMS,GENERIC_NAME")
            }.body()

            if (response.size < 4) return emptyList()

            val names = response[1].jsonArray
            val detailsGroups = response[3].jsonArray

            val suggestions = mutableListOf<MedicationSuggestion>()

            for (i in 0 until names.size) {
                val fullName = names[i].jsonPrimitive.content
                // RxTerms often returns "Brand (Generic)". Let's extract the clean brand name.
                val cleanName = if (fullName.contains("(")) {
                    fullName.substringBefore("(").trim()
                } else {
                    fullName
                }
                
                val details = detailsGroups[i].jsonArray
                if (details.isEmpty()) {
                    suggestions.add(createWithHeuristics(cleanName))
                } else {
                    val strengths = details[0].jsonArray
                    val genericName = details.getOrNull(1)?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                    
                    if (strengths.isEmpty()) {
                        suggestions.add(createWithHeuristics(cleanName, genericName = genericName))
                    } else {
                        // Just take the first few strengths to avoid overwhelming the UI
                        strengths.take(2).forEach { strengthElement ->
                            val strengthStr = strengthElement.jsonPrimitive.content
                            suggestions.add(parseComprehensive(cleanName, strengthStr, genericName))
                        }
                    }
                }
            }
            suggestions.distinctBy { "${it.name}-${it.doseAmount}-${it.doseUnit}-${it.genericName}" }.take(8)
        } catch (e: Exception) {
            android.util.Log.e("SuggestionRepo", "API Error", e)
            emptyList()
        }
    }

    private fun parseComprehensive(name: String, strengthAndForm: String, genericName: String?): MedicationSuggestion {
        // strengthAndForm is like "200 MG Tab" or "500 MG / 5 ML Susp"
        val regex = """(\d+\.?\d*)\s*([a-zA-Z]+)""".toRegex()
        val match = regex.find(strengthAndForm)
        
        val amount = match?.groupValues?.get(1)?.toDoubleOrNull()
        val unit = match?.groupValues?.get(2)?.lowercase() ?: "tablet"

        return createWithHeuristics(
            name = name,
            genericName = genericName,
            doseAmount = amount,
            doseUnit = unit
        )
    }

    private fun createWithHeuristics(
        name: String,
        genericName: String? = null,
        doseAmount: Double? = null,
        doseUnit: String = "tablet"
    ): MedicationSuggestion {
        val lowerName = name.lowercase()
        return when {
            lowerName.contains("ibuprofen") || lowerName.contains("advil") || lowerName.contains("motrin") -> {
                MedicationSuggestion(
                    name = name,
                    genericName = genericName ?: "Ibuprofen",
                    doseAmount = doseAmount,
                    doseUnit = doseUnit,
                    frequencyHint = Frequency.HOURLY,
                    hourlyIntervalHint = 4,
                    usageNote = "Take with food or milk",
                    warning = "May cause stomach bleeding"
                )
            }
            lowerName.contains("paracetamol") || lowerName.contains("acetaminophen") || lowerName.contains("tylenol") -> {
                MedicationSuggestion(
                    name = name,
                    genericName = genericName ?: "Paracetamol",
                    doseAmount = doseAmount,
                    doseUnit = doseUnit,
                    frequencyHint = Frequency.HOURLY,
                    hourlyIntervalHint = 4,
                    usageNote = "Do not exceed 4g per day",
                    warning = "Can cause severe liver damage"
                )
            }
            lowerName.contains("aspirin") -> {
                MedicationSuggestion(
                    name = name,
                    genericName = genericName ?: "Aspirin",
                    doseAmount = doseAmount,
                    doseUnit = doseUnit,
                    frequencyHint = Frequency.DAILY,
                    dailyIntervalHint = 1,
                    usageNote = "Take with a full glass of water",
                    warning = "Risk of Reye's syndrome in children"
                )
            }
            lowerName.contains("amoxicillin") || lowerName.contains("antibiotic") -> {
                MedicationSuggestion(
                    name = name,
                    genericName = genericName ?: "Amoxicillin",
                    doseAmount = doseAmount,
                    doseUnit = doseUnit,
                    frequencyHint = Frequency.HOURLY,
                    hourlyIntervalHint = 8,
                    usageNote = "Finish the entire course",
                    warning = "May cause allergic reaction"
                )
            }
            // Add a default for others
            else -> MedicationSuggestion(name, genericName, doseAmount, doseUnit)
        }
    }
}
