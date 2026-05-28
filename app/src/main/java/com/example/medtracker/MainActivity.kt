package com.example.medtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.example.medtracker.ui.AppNav
import com.example.medtracker.ui.LocalRepository
import com.example.medtracker.ui.theme.MedTrackerTheme
import com.example.medtracker.util.EnsurePostNotificationsPermission

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

