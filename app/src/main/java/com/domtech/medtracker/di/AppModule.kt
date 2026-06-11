package com.domtech.medtracker.di

import android.content.Context
import com.domtech.medtracker.data.AppDatabase
import com.domtech.medtracker.data.MedRepository
import com.domtech.medtracker.reminders.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.build(context)
    }

    @Provides
    @Singleton
    fun provideMedRepository(db: AppDatabase): MedRepository {
        return MedRepository(db)
    }

    @Provides
    @Singleton
    fun provideReminderScheduler(
        @ApplicationContext context: Context,
        repo: MedRepository
    ): ReminderScheduler {
        return ReminderScheduler(context, repo)
    }
}