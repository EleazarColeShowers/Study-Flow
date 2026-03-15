package com.el.planora.di

import android.content.Context
import androidx.room.Room
import com.el.planora.data.local.PlanoraDatabase
import com.el.planora.data.local.dao.CheckInSummaryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.jvm.java

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PlanoraDatabase =
        Room.databaseBuilder(
            context,
            PlanoraDatabase::class.java,
            "studyflow.db"
        ).build()

    @Provides
    @Singleton
    fun provideCheckInSummaryDao(db: PlanoraDatabase): CheckInSummaryDao =
        db.checkInSummaryDao()
}
