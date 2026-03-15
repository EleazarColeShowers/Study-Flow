package com.el.planora.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checkin_summaries")
data class CheckInSummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subject: String,
    val quizScore: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val understanding: String,
    val nextReviewDate: String,   // "YYYY-MM-DD" — used by calendar feature
    val focusQuality: Int,
    val energyLevel: Int,
    val needsReview: Boolean,
    val encouragement: String,
    val savedAt: Long = System.currentTimeMillis()
)
