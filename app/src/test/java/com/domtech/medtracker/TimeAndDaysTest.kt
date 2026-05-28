package com.domtech.medtracker

import androidx.test.core.app.ApplicationProvider
import com.domtech.medtracker.ui.util.Days
import com.domtech.medtracker.ui.util.formatMinutesOfDay
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TimeAndDaysTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun formatMinutesOfDay_zeroPadsAndClamps() {
        assertThat(formatMinutesOfDay(0)).isEqualTo("00:00")
        assertThat(formatMinutesOfDay(9 * 60 + 5)).isEqualTo("09:05")
        assertThat(formatMinutesOfDay(24 * 60 + 999)).isEqualTo("23:59")
    }

    @Test
    fun days_toggleAndFormatMask() {
        var mask = 0
        mask = Days.toggle(mask, Days.all.first { it.shortLabel == "Mon" }.bit)
        mask = Days.toggle(mask, Days.all.first { it.shortLabel == "Wed" }.bit)
        assertThat(Days.formatMask(context, mask)).isEqualTo("Mon, Wed")

        mask = Days.toggle(mask, Days.all.first { it.shortLabel == "Mon" }.bit)
        assertThat(Days.formatMask(context, mask)).isEqualTo("Wed")
    }
}
