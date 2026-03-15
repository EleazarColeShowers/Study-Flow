package com.el.planora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.el.planora.data.local.dao.CheckInSummaryDao
import com.el.planora.data.local.entity.CheckInSummaryEntity

@Database(
    entities = [CheckInSummaryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PlanoraDatabase : RoomDatabase() {
    abstract fun checkInSummaryDao(): CheckInSummaryDao
}
