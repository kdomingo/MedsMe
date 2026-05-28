package com.example.medtracker.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.medtracker.data.MedRepository

val LocalRepository = staticCompositionLocalOf<MedRepository> {
    error("MedRepository not provided")
}

