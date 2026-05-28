package com.domtech.medtracker.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.domtech.medtracker.data.MedRepository

val LocalRepository = staticCompositionLocalOf<MedRepository> {
    error("MedRepository not provided")
}

