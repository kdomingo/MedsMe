package com.domtech.medtracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MedGreenPrimaryDark,
    onPrimary = MedGreenOnPrimaryDark,
    primaryContainer = MedGreenContainerDark,
    onPrimaryContainer = MedGreenOnContainerDark,
    secondary = MedRedSecondaryDark,
    onSecondary = MedRedOnSecondaryDark,
    secondaryContainer = MedRedContainerDark,
    onSecondaryContainer = MedRedOnSecondaryContainerDark,
    error = MedErrorDark,
    onError = MedOnErrorDark,
    errorContainer = MedErrorContainerDark,
    onErrorContainer = MedOnErrorContainerDark,
    background = MedBackgroundDark,
    onBackground = MedOnBackgroundDark,
    surface = MedSurfaceDark,
    onSurface = MedOnSurfaceDark,
    surfaceVariant = MedSurfaceVariantDark,
    onSurfaceVariant = MedOnSurfaceVariantDark,
    outline = MedOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = MedGreenPrimary,
    onPrimary = MedGreenOnPrimary,
    primaryContainer = MedGreenContainer,
    onPrimaryContainer = MedGreenOnContainer,
    secondary = MedRedSecondary,
    onSecondary = MedRedOnSecondary,
    secondaryContainer = MedRedContainer,
    onSecondaryContainer = MedRedOnSecondaryContainer,
    error = MedError,
    onError = MedOnError,
    errorContainer = MedErrorContainer,
    onErrorContainer = MedOnErrorContainer,
    background = MedBackground,
    onBackground = MedOnBackground,
    surface = MedSurface,
    onSurface = MedOnSurface,
    surfaceVariant = MedSurfaceVariant,
    onSurfaceVariant = MedOnSurfaceVariant,
    outline = MedOutline
)

@Composable
fun MedsMeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled to enforce the Green/Red medical theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
