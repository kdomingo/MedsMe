package com.domtech.medtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.domtech.medtracker.ui.AppNav
import com.domtech.medtracker.ui.theme.MedTrackerTheme
import com.domtech.medtracker.util.EnsureExactAlarmPermission
import com.domtech.medtracker.util.EnsurePostNotificationsPermission
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedTrackerTheme {
                val focusManager = LocalFocusManager.current
                EnsurePostNotificationsPermission()
                EnsureExactAlarmPermission()
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            focusManager.clearFocus()
                        })
                    }
                ) {
                    AppNav()
                }
            }
        }
    }
}