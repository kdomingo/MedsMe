package com.domtech.medtracker.ui.util

import androidx.test.core.app.ApplicationProvider
import com.domtech.medtracker.data.Frequency
import com.domtech.medtracker.data.MedicationEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FormatUtilsTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun `formatDose correctly pluralizes tablets`() {
        assertThat(FormatUtils.formatDose(1.0, "tablet")).isEqualTo("1 tablet")
        assertThat(FormatUtils.formatDose(2.0, "tablet")).isEqualTo("2 tablets")
    }

    @Test
    fun `formatDose handles decimals correctly`() {
        assertThat(FormatUtils.formatDose(1.5, "tablet")).isEqualTo("1.5 tablets")
    }

    @Test
    fun `formatDose does not pluralize mg or ml`() {
        assertThat(FormatUtils.formatDose(500.0, "mg")).isEqualTo("500 mg")
        assertThat(FormatUtils.formatDose(5.0, "ml")).isEqualTo("5 ml")
    }

    @Test
    fun `getNextDoseWarning returns null if never taken`() {
        val med = createMed(frequency = Frequency.HOURLY, lastTaken = 0)
        assertThat(FormatUtils.getNextDoseWarning(context, med, System.currentTimeMillis())).isNull()
    }

    @Test
    fun `getNextDoseWarning returns warning for daily medication`() {
        val now = 100000000L
        val lastTaken = now - 12 * 3600_000L // 12 hours ago
        val med = createMed(frequency = Frequency.DAILY, dailyInterval = 1, lastTaken = lastTaken)
        val warning = FormatUtils.getNextDoseWarning(context, med, now)
        assertThat(warning).contains("12 hours")
    }

    @Test
    fun `getNextDoseWarning returns warning for multi-day medication`() {
        val now = 100000000L
        val lastTaken = now - 24 * 3600_000L // 1 day ago
        val med = createMed(frequency = Frequency.DAILY, dailyInterval = 2, lastTaken = lastTaken)
        val warning = FormatUtils.getNextDoseWarning(context, med, now)
        assertThat(warning).contains("1 day")
    }

    @Test
    fun `getNextDoseWarning returns null if daily interval passed`() {
        val now = 100000000L
        val lastTaken = now - 25 * 3600_000L // 25 hours ago
        val med = createMed(frequency = Frequency.DAILY, dailyInterval = 1, lastTaken = lastTaken)
        assertThat(FormatUtils.getNextDoseWarning(context, med, now)).isNull()
    }

    @Test
    fun `getNextDoseWarning returns null if interval passed`() {
        val now = 1000000L
        val lastTaken = now - 5 * 3600_000L // 5 hours ago
        val med = createMed(frequency = Frequency.HOURLY, interval = 4, lastTaken = lastTaken)
        assertThat(FormatUtils.getNextDoseWarning(context, med, now)).isNull()
    }

    @Test
    fun `getNextDoseWarning returns warning if taken recently`() {
        val now = 1000000L
        val lastTaken = now - 2 * 3600_000L // 2 hours ago
        val med = createMed(frequency = Frequency.HOURLY, interval = 4, lastTaken = lastTaken)
        val warning = FormatUtils.getNextDoseWarning(context, med, now)
        assertThat(warning).contains("2 hours")
    }

    @Test
    fun `getNextDoseWarning handles minutes in warning`() {
        val now = 1000000L
        val lastTaken = now - (1 * 3600_000L + 30 * 60_000L) // 1.5 hours ago
        val med = createMed(frequency = Frequency.HOURLY, interval = 4, lastTaken = lastTaken)
        val warning = FormatUtils.getNextDoseWarning(context, med, now)
        assertThat(warning).contains("2 hours and 30 minutes")
    }

    private fun createMed(
        frequency: Frequency = Frequency.DAILY,
        interval: Int = 0,
        dailyInterval: Int = 1,
        lastTaken: Long = 0
    ) = MedicationEntity(
        id = 1,
        name = "Test",
        doseAmount = 1.0,
        doseUnit = "tablet",
        frequency = frequency,
        hourlyInterval = interval,
        dailyInterval = dailyInterval,
        notes = "",
        currentLevel = 10.0,
        lowLevelThreshold = 2.0,
        lastTakenEpochMs = lastTaken,
        updatedAtEpochMs = 0
    )
}
