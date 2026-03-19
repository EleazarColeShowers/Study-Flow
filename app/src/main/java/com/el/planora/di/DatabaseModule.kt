package com.el.planora.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.el.planora.data.local.PlanoraDatabase
import com.el.planora.data.local.dao.CheckInSummaryDao
import com.el.planora.data.local.dao.CompletedSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS completed_sessions (
                    sessionKey TEXT NOT NULL PRIMARY KEY,
                    subjectId TEXT NOT NULL,
                    date TEXT NOT NULL,
                    sessionIndex INTEGER NOT NULL,
                    completedAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlanoraDatabase =
        Room.databaseBuilder(
            context,
            PlanoraDatabase::class.java,
            "studyflow.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideCheckInSummaryDao(db: PlanoraDatabase): CheckInSummaryDao =
        db.checkInSummaryDao()

    @Provides
    fun provideCompletedSessionDao(db: PlanoraDatabase): CompletedSessionDao =
        db.completedSessionDao()
}