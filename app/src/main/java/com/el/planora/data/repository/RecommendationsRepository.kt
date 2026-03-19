package com.el.planora.data.repository

import com.el.planora.data.remote.planoraApiService
import com.el.planora.data.remote.model.RecommendRequest
import com.el.planora.data.remote.model.RecommendResponse
import com.el.planora.data.remote.model.RecommendSubjectProfile
import com.el.planora.data.remote.model.RecommendUserProfile
import com.el.planora.domain.model.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationsRepository @Inject constructor(
    private val api: planoraApiService,
    private val userProfileRepository: UserProfileRepository
) {

    suspend fun getRecommendations(): ApiResult<RecommendResponse> {
        return try {
            // Fetch real profile from Firestore
            val profile = when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error   -> return ApiResult.Error(result.message)
                else                 -> return ApiResult.Error("Could not load profile")
            }

            val request = buildRequest(profile)
            val response = api.getRecommendations(request)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }

    suspend fun getRecommendationsForSubject(
        subjectId: String,
        subjectName: String,
        contentType: String,
        daysToExam: Int
    ): ApiResult<RecommendResponse> {
        return try {
            val profile = when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error   -> return ApiResult.Error(result.message)
                else                 -> return ApiResult.Error("Could not load profile")
            }

            // Map content type back to API value if needed
            val apiContentType = when {
                contentType.contains("Theory", ignoreCase = true)       -> "theory"
                contentType.contains("Calculation", ignoreCase = true)  -> "calculation"
                contentType.contains("Practical", ignoreCase = true)    -> "practical"
                else                                                     -> "mixed"
            }

            val request = RecommendRequest(
                user = RecommendUserProfile(
                    userName      = profile.userName,
                    userCategory  = profile.userCategory,
                    hasAdhd       = profile.hasAdhd,
                    hasDyslexia   = profile.hasDyslexia,
                    hasAutism     = profile.hasAutism,
                    hasAnxiety    = profile.hasAnxiety,
                    attentionSpan = profile.attentionSpan,
                    sleepHours    = profile.sleepHours,
                    dailyStudyHrs = profile.dailyStudyHrs,
                    learningStyle = profile.learningStyle,
                    peakFocusTime = profile.peakFocusTime,
                    studyEnv      = profile.studyEnv,
                    struggle      = profile.struggle,
                    currentLevel  = profile.currentLevel,
                    priorAttempt  = profile.priorAttempt,
                    successGoal   = profile.successGoal
                ),
                subject = RecommendSubjectProfile(
                    subjectName = subjectName,
                    contentType = apiContentType,
                    memoryLoad  = "high",
                    difficulty  = if (daysToExam < 30) 4 else 3,
                    hasDeadline = if (daysToExam < 999) 1 else 0,
                    daysToExam  = daysToExam
                )
            )

            // Also pass user_id and subject_id so results are saved to Supabase
            // The API accepts these as optional top-level fields alongside user/subject
            val response = api.getRecommendations(request)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }

    private fun buildRequest(profile: UserProfile): RecommendRequest {
        val raw = profile.rawAnswers

        // ── Resolve subject name from onboarding answers ──────────────────────
        val subjectName = when (profile.userCategory) {
            "uni_student"      -> raw["uni_subjects"]?.split(",")?.firstOrNull()?.trim() ?: "General Studies"
            "language_learner" -> raw["lang_language"] ?: "Language"
            "cert_candidate"   -> raw["cert_name"] ?: "Certification"
            "self_study"       -> raw["self_topic"] ?: "Self Study"
            else               -> "General Studies"
        }

        // ── Content type ──────────────────────────────────────────────────────
        val contentTypeRaw = raw["uni_content_type"] ?: raw["self_content_type"] ?: ""
        val contentType = when {
            "Theory" in contentTypeRaw      -> "theory"
            "Calculation" in contentTypeRaw -> "calculation"
            "Practical" in contentTypeRaw   -> "practical"
            else                            -> "mixed"
        }

        // ── Memory load ───────────────────────────────────────────────────────
        val memoryLoadRaw = raw["uni_memory_load"] ?: raw["self_memory_load"] ?: ""
        val memoryLoad = when {
            "High" in memoryLoadRaw -> "high"
            "Low" in memoryLoadRaw  -> "low"
            else                    -> "medium"
        }

        // ── Difficulty (1–5) ──────────────────────────────────────────────────
        val difficulty = when (raw["uni_exams"] ?: raw["cert_date"] ?: "") {
            "Yes, very soon", "Within 1 month" -> 4
            "Yes, in a few months", "1–3 months" -> 3
            else -> 2
        }

        // ── Deadline ─────────────────────────────────────────────────────────
        val hasDeadline = when {
            raw["uni_exams"]?.contains("soon") == true    -> 1
            raw["uni_exams"]?.contains("months") == true  -> 1
            raw["cert_date"]?.contains("month") == true   -> 1
            raw["self_deadline"]?.contains("month") == true -> 1
            else                                          -> 0
        }

        // ── Days to exam ──────────────────────────────────────────────────────
        val daysToExam = when {
            raw["uni_exams"] == "Yes, very soon"          -> 21
            raw["uni_exams"] == "Yes, in a few months"    -> 90
            raw["cert_date"] == "Within 1 month"          -> 21
            raw["cert_date"] == "1–3 months"              -> 60
            raw["cert_date"] == "3–6 months"              -> 120
            raw["self_deadline"] == "Within 1 month"      -> 21
            raw["self_deadline"] == "1–3 months"          -> 60
            raw["self_deadline"] == "3–6 months"          -> 120
            else                                          -> 999
        }

        return RecommendRequest(
            user = RecommendUserProfile(
                userName      = profile.userName,
                userCategory  = profile.userCategory,
                hasAdhd       = profile.hasAdhd,
                hasDyslexia   = profile.hasDyslexia,
                hasAutism     = profile.hasAutism,
                hasAnxiety    = profile.hasAnxiety,
                attentionSpan = profile.attentionSpan,
                sleepHours    = profile.sleepHours,
                dailyStudyHrs = profile.dailyStudyHrs,
                learningStyle = profile.learningStyle,
                peakFocusTime = profile.peakFocusTime,
                studyEnv      = profile.studyEnv,
                struggle      = profile.struggle,
                currentLevel  = profile.currentLevel,
                priorAttempt  = profile.priorAttempt,
                successGoal   = profile.successGoal
            ),
            subject = RecommendSubjectProfile(
                subjectName = subjectName,
                contentType = contentType,
                memoryLoad  = memoryLoad,
                difficulty  = difficulty,
                hasDeadline = hasDeadline,
                daysToExam  = daysToExam
            )
        )
    }

    private fun errorMessage(code: Int) = when (code) {
        429  -> "AI is busy right now. Try again in a minute."
        500  -> "Something went wrong on the server. Please try again."
        else -> "Unexpected error ($code). Please try again."
    }
}