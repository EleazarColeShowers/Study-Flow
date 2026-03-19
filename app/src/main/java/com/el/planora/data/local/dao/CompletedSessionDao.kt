package com.el.planora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.el.planora.data.local.entity.CompletedSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markComplete(session: CompletedSessionEntity)

    @Query("DELETE FROM completed_sessions WHERE sessionKey = :key")
    suspend fun markIncomplete(key: String)

    @Query("SELECT sessionKey FROM completed_sessions WHERE subjectId = :subjectId AND date = :date")
    fun getCompletedKeysForDate(subjectId: String, date: String): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM completed_sessions WHERE subjectId = :subjectId")
    suspend fun getTotalCompletedForSubject(subjectId: String): Int
}