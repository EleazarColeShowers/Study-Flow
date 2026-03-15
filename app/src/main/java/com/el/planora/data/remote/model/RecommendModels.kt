package com.el.planora.data.remote.model

import com.google.gson.annotations.SerializedName

// ── POST /recommend ───────────────────────────────────────────────────────────

data class RecommendRequest(
    val user: RecommendUserProfile,
    val subject: RecommendSubjectProfile
)

data class RecommendUserProfile(
    @SerializedName("user_name")      val userName: String,
    @SerializedName("user_category")  val userCategory: String,
    @SerializedName("has_adhd")       val hasAdhd: Int,
    @SerializedName("has_dyslexia")   val hasDyslexia: Int,
    @SerializedName("has_autism")     val hasAutism: Int,
    @SerializedName("has_anxiety")    val hasAnxiety: Int,
    @SerializedName("attention_span") val attentionSpan: String,
    @SerializedName("sleep_hours")    val sleepHours: Float,
    @SerializedName("daily_study_hrs") val dailyStudyHrs: Float,
    @SerializedName("learning_style") val learningStyle: String,
    @SerializedName("peak_focus_time") val peakFocusTime: String,
    @SerializedName("study_env")      val studyEnv: String,
    val struggle: String,
    @SerializedName("current_level")  val currentLevel: String,
    @SerializedName("prior_attempt")  val priorAttempt: String,
    @SerializedName("success_goal")   val successGoal: String
)

data class RecommendSubjectProfile(
    @SerializedName("subject_name")   val subjectName: String,
    @SerializedName("content_type")   val contentType: String,
    @SerializedName("memory_load")    val memoryLoad: String,
    val difficulty: Int,
    @SerializedName("has_deadline")   val hasDeadline: Int,
    @SerializedName("days_to_exam")   val daysToExam: Int
)

// ── Response ──────────────────────────────────────────────────────────────────

data class RecommendResponse(
    val subject: String?,
    @SerializedName("session_length")  val sessionLength: String?,
    @SerializedName("daily_sessions")  val dailySessions: String?,
    val recommendations: List<RecommendationItem>?
)

data class RecommendationItem(
    val rank: Int?,
    val id: String?,
    val name: String?,
    @SerializedName("how_to") val howTo: String?,
    val why: String?,
    val confidence: Float?
)