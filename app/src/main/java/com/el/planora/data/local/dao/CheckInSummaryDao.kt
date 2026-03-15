package com.el.planora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.el.planora.data.local.entity.CheckInSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: CheckInSummaryEntity)

    // All summaries — newest first. Used by calendar + history screens later.
    @Query("SELECT * FROM checkin_summaries ORDER BY savedAt DESC")
    fun getAllSummaries(): Flow<List<CheckInSummaryEntity>>

    // Summaries due for review on a specific date — used by calendar feature.
    @Query("SELECT * FROM checkin_summaries WHERE nextReviewDate = :date ORDER BY savedAt DESC")
    fun getSummariesDueOn(date: String): Flow<List<CheckInSummaryEntity>>

    @Query("DELETE FROM checkin_summaries WHERE id = :id")
    suspend fun delete(id: Int)
}
