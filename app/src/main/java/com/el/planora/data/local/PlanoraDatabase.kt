package com.el.planora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.el.planora.data.local.dao.CheckInSummaryDao
import com.el.planora.data.local.dao.CompletedSessionDao
import com.el.planora.data.local.entity.CheckInSummaryEntity
import com.el.planora.data.local.entity.CompletedSessionEntity

@Database(
    entities = [
        CheckInSummaryEntity::class,
        CompletedSessionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PlanoraDatabase : RoomDatabase() {
    abstract fun checkInSummaryDao(): CheckInSummaryDao
    abstract fun completedSessionDao(): CompletedSessionDao
}
