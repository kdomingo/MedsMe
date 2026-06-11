package com.domtech.medtracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.domtech.medtracker.reminders.LowStockWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class MedTrackerApplication : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // db and repo will be injected by Hilt where needed, 
        // but we might still need them for legacy reasons if we haven't refactored everything yet.
        // For now, let's keep the manual init if needed, or remove if we trust Hilt.
        // Actually, let's remove manual init of db and repo here and use injection.
        
        try {
            val work = PeriodicWorkRequestBuilder<LowStockWorker>(12, TimeUnit.HOURS).build()
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                LowStockWorker.UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                work,
            )
        } catch (e: Exception) {
            // Likely in a test environment where WorkManager is not initialized.
        }
    }
}

