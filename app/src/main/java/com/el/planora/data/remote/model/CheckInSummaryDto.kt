package com.el.planora.data.remote.model

import com.google.gson.annotations.SerializedName

// ── Ping ──────────────────────────────────────────────────────────────────────

data class PingResponse(
    val status: String,
    val api: String
)

// ── Check-in: Start ───────────────────────────────────────────────────────────

data class CheckInStartRequest(
    @SerializedName("user_id")    val userId: String,
    @SerializedName("user_profile") val userProfile: CheckInUserProfile,
    @SerializedName("subject_name") val subjectName: String,
    val material: String? = null
)

data class CheckInUserProfile(
    @SerializedName("user_name")      val userName: String,
    @SerializedName("user_category")  val userCategory: String,
    @SerializedName("has_adhd")       val hasAdhd: Int,
    @SerializedName("has_dyslexia")   val hasDyslexia: Int,
    @SerializedName("has_autism")     val hasAutism: Int,
    @SerializedName("has_anxiety")    val hasAnxiety: Int,
    val struggle: String,
    @SerializedName("success_goal")   val successGoal: String
)

data class CheckInStartResponse(
    val message: String,
    val stage: String,
    @SerializedName("user_id") val userId: String
)

// ── Check-in: Message ─────────────────────────────────────────────────────────

data class CheckInMessageRequest(
    @SerializedName("user_id") val userId: String,
    val message: String
)

data class CheckInMessageResponse(
    val message: String,
    val stage: String,           // "checkin" | "quiz" | "complete"
    val summary: CheckInSummaryDto? = null
)

// ── Check-in: Summary (inside complete response) ──────────────────────────────

data class CheckInSummaryDto(
    val subject: String,
    @SerializedName("quiz_score")       val quizScore: Int,
    @SerializedName("correct_answers")  val correctAnswers: Int,
    @SerializedName("total_questions")  val totalQuestions: Int,
    val understanding: String,
    @SerializedName("next_review_date") val nextReviewDate: String,
    @SerializedName("focus_quality")    val focusQuality: Int,
    @SerializedName("energy_level")     val energyLevel: Int,
    @SerializedName("needs_review")     val needsReview: Boolean,
    val encouragement: String
)