package com.el.planora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "completed_sessions")
data class CompletedSessionEntity(
    @PrimaryKey
    val sessionKey: String,   // "{subjectId}_{date}_{sessionIndex}" e.g. "abc_2026-03-19_0"
    val subjectId: String,
    val date: String,         // "YYYY-MM-DD"
    val sessionIndex: Int,
    val completedAt: Long = System.currentTimeMillis()
)