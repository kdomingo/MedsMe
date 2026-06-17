package com.domtech.medtracker.di

import android.content.Context
import com.domtech.medtracker.data.AppDatabase
import com.domtech.medtracker.data.MedRepository
import com.domtech.medtracker.data.repository.SuggestionRepositoryImpl
import com.domtech.medtracker.domain.repository.SuggestionRepository
import com.domtech.medtracker.reminders.ReminderScheduler
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSuggestionRepository(impl: SuggestionRepositoryImpl): SuggestionRepository

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
            return AppDatabase.build(context)
        }

        @Provides
        @Singleton
        fun provideHttpClient(): HttpClient {
            return HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    })
                }
                install(Logging) {
                    level = LogLevel.INFO
                    logger = object : Logger {
                        override fun log(message: String) {
                            android.util.Log.d("HttpClient", message)
                        }
                    }
                }
            }
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
}