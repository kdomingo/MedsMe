package com.domtech.medtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.domtech.medtracker.ui.AppNav
import com.domtech.medtracker.ui.LocalRepository
import com.domtech.medtracker.ui.theme.MedTrackerTheme
import com.domtech.medtracker.util.EnsurePostNotificationsPermission

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedTrackerTheme {
                val app = (LocalContext.current.applicationContext as MedTrackerApplication)
                CompositionLocalProvider(LocalRepository provides app.repo) {
                    EnsurePostNotificationsPermission()
                    Surface(color = MaterialTheme.colorScheme.background) {
                        AppNav()
                    }
                }
            }
        }
    }
}

