package com.domtech.medtracker.data.repository

import com.domtech.medtracker.data.Frequency
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestionRepositoryImplTest {

    private fun createRepository(mockJson: String): SuggestionRepositoryImpl {
        val mockEngine = MockEngine { _ ->
            respond(
                content = mockJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        return SuggestionRepositoryImpl(client)
    }

    @Test
    fun `getSuggestions parses NIH API format correctly`() = runTest {
        // NIH API format with extra fields STRENGTHS_AND_FORMS, GENERIC_NAME
        // [count, [names], null, [ [ [strengths], generic_name ], ... ]]
        val mockJson = """
            [1, ["Ibuprofen"], null, [[["200 MG Tab", "400 MG Tab"], "Ibuprofen"]]]
        """.trimIndent()
        
        val repository = createRepository(mockJson)
        val results = repository.getSuggestions("Ibu")
        
        // We take up to 2 strengths per name in implementation
        assertEquals(2, results.size)
        assertEquals("Ibuprofen", results[0].name)
        assertEquals(200.0, results[0].doseAmount!!, 0.01)
        assertEquals("mg", results[0].doseUnit)
        assertEquals(Frequency.HOURLY, results[0].frequencyHint)
        assertEquals(4, results[0].hourlyIntervalHint)
    }

    @Test
    fun `getSuggestions applies heuristics for common medications`() = runTest {
        val mockJson = """
            [1, ["Amoxicillin"], null, [[["500 MG Cap"], "Amoxicillin"]]]
        """.trimIndent()
        
        val repository = createRepository(mockJson)
        val results = repository.getSuggestions("Amox")
        
        assertEquals(1, results.size)
        assertEquals(Frequency.HOURLY, results[0].frequencyHint)
        assertEquals(8, results[0].hourlyIntervalHint)
    }

    @Test
    fun `getSuggestions returns generic name for brand name search`() = runTest {
        val mockJson = """
            [1, ["Lipitor"], null, [[["10 MG Tab"], "Atorvastatin"]]]
        """.trimIndent()
        
        val repository = createRepository(mockJson)
        val results = repository.getSuggestions("Lipi")
        
        assertEquals(1, results.size)
        assertEquals("Lipitor", results[0].name)
        assertEquals("Atorvastatin", results[0].genericName)
    }

    @Test
    fun `getSuggestions returns usage notes and warnings for Ibuprofen`() = runTest {
        val mockJson = """
            [1, ["Ibuprofen"], null, [[["200 MG Tab"], "Ibuprofen"]]]
        """.trimIndent()
        
        val repository = createRepository(mockJson)
        val results = repository.getSuggestions("Ibu")
        
        assertEquals(1, results.size)
        assertEquals("Take with food or milk", results[0].usageNote)
        assertEquals("May cause stomach bleeding", results[0].warning)
    }

    @Test
    fun `getSuggestions handles empty response gracefully`() = runTest {
        val mockJson = """
            [0, [], null, []]
        """.trimIndent()
        
        val repository = createRepository(mockJson)
        val results = repository.getSuggestions("Unknown")
        
        assertTrue(results.isEmpty())
    }
}
