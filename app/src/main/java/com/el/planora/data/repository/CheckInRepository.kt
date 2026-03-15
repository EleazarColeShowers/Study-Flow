package com.el.planora.data.repository

import com.el.planora.data.local.dao.CheckInSummaryDao
import com.el.planora.data.local.entity.CheckInSummaryEntity
import com.el.planora.data.mapper.UserProfileMapper
import com.el.planora.data.remote.PlonoraApiService
import com.el.planora.data.remote.model.CheckInMessageRequest
import com.el.planora.data.remote.model.CheckInMessageResponse
import com.el.planora.data.remote.model.CheckInStartResponse
import com.el.planora.data.remote.model.CheckInSummaryDto
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

data class CheckInStartRequestFlexible(
    val user_id: String,
    val user_profile: Map<String, Any>,
    val subject_name: String,
    val material: String? = null
)

@Singleton
class CheckInRepository @Inject constructor(
    private val api: PlonoraApiService,
    private val summaryDao: CheckInSummaryDao,
    private val userProfileRepository: UserProfileRepository  // ← real profile
) {

    // ── Ping: wake the server ─────────────────────────────────────────────────
    suspend fun ping(): ApiResult<Unit> {
        return try {
            val response = api.ping()
            if (response.isSuccessful) ApiResult.Success(Unit)
            else ApiResult.Error("Server returned ${response.code()}")
        } catch (e: Exception) {
            try {
                delay(60_000)
                val retry = api.ping()
                if (retry.isSuccessful) ApiResult.Success(Unit)
                else ApiResult.Error("Server unavailable after retry")
            } catch (e2: Exception) {
                ApiResult.Error("No internet connection")
            }
        }
    }

    // ── Start check-in with real user profile from Firestore ──────────────────
    suspend fun startCheckIn(
        userId: String,
        subjectName: String,
        material: String? = null
    ): ApiResult<CheckInStartResponse> {
        return try {
            // Fetch real profile — fall back to placeholder if unavailable
            val profileMap = when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> UserProfileMapper.toApiMap(result.data)
                else                 -> fallbackProfile()
            }

            val response = api.startCheckInFlexible(
                CheckInStartRequestFlexible(
                    user_id      = userId,
                    subject_name = subjectName,
                    material     = material,
                    user_profile = profileMap
                )
            )

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }

    // ── Send a message ────────────────────────────────────────────────────────
    suspend fun sendMessage(
        userId: String,
        message: String
    ): ApiResult<CheckInMessageResponse> {
        return try {
            val response = api.sendMessage(
                CheckInMessageRequest(userId = userId, message = message)
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                body.summary?.let { saveSummary(it) }
                ApiResult.Success(body)
            } else if (response.code() == 404) {
                ApiResult.Error("SESSION_EXPIRED")
            } else {
                ApiResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }

    // ── End session cleanly ───────────────────────────────────────────────────
    suspend fun endCheckIn(userId: String) {
        try { api.endCheckIn(userId) } catch (_: Exception) { }
    }

    // ── Persist summary to Room ───────────────────────────────────────────────
    private suspend fun saveSummary(dto: CheckInSummaryDto) {
        summaryDao.insert(
            CheckInSummaryEntity(
                subject        = dto.subject,
                quizScore      = dto.quizScore,
                correctAnswers = dto.correctAnswers,
                totalQuestions = dto.totalQuestions,
                understanding  = dto.understanding,
                nextReviewDate = dto.nextReviewDate,
                focusQuality   = dto.focusQuality,
                energyLevel    = dto.energyLevel,
                needsReview    = dto.needsReview,
                encouragement  = dto.encouragement
            )
        )
    }

    // ── Fallback if Firestore fetch fails ─────────────────────────────────────
    private fun fallbackProfile(): Map<String, Any> = mapOf(
        "user_name"       to "Student",
        "user_category"   to "uni_student",
        "has_adhd"        to 0,
        "has_dyslexia"    to 0,
        "has_autism"      to 0,
        "has_anxiety"     to 0,
        "attention_span"  to "20_45",
        "sleep_hours"     to 7.0,
        "daily_study_hrs" to 2.0,
        "learning_style"  to "read_write",
        "peak_focus_time" to "morning",
        "study_env"       to "varies",
        "struggle"        to "understanding",
        "current_level"   to "not_applicable",
        "prior_attempt"   to "not_applicable",
        "success_goal"    to "do well in my exams"
    )

    private fun errorMessage(code: Int) = when (code) {
        429  -> "AI coach is busy right now. Try again in a minute."
        500  -> "Something went wrong. Please try again."
        else -> "Unexpected error ($code). Please try again."
    }
}