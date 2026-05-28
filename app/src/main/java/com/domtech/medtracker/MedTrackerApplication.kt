package com.domtech.medtracker

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.domtech.medtracker.data.AppDatabase
import com.domtech.medtracker.data.MedRepository
import com.domtech.medtracker.reminders.LowStockWorker
import java.util.concurrent.TimeUnit

class MedTrackerApplication : Application() {
    lateinit var db: AppDatabase
        private set
    lateinit var repo: MedRepository
        private set

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.build(this)
        repo = MedRepository(db)

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

