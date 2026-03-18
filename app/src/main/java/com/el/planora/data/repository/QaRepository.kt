package com.el.planora.data.repository

import com.el.planora.data.remote.PlonoraApiService
import com.el.planora.data.remote.model.QaResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QaRepository @Inject constructor(
    private val api: PlonoraApiService,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) {

    suspend fun askQuestion(question: String, context: String? = null): ApiResult<QaResponse> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return ApiResult.Error("Not logged in")

            // Fetch real profile for subject + category + conditions
            val profile = when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error   -> return ApiResult.Error(result.message)
                else                 -> return ApiResult.Error("Could not load profile")
            }

            val subjectName = resolveSubjectName(profile.rawAnswers, profile.userCategory)

            // V2: /qa now accepts a JSON body instead of query parameters
            val requestBody = JsonObject().apply {
                addProperty("user_id", userId)
                addProperty("question", question)
                addProperty("subject_name", subjectName)
                addProperty("user_category", profile.userCategory)

                // Optional: pass study material as context for grounded answers
                if (context != null) addProperty("context", context)

                // Optional: user_profile makes responses condition-aware
                // e.g. shorter answers for ADHD, simpler language for dyslexia
                add("user_profile", JsonObject().apply {
                    addProperty("has_adhd", profile.hasAdhd)
                    addProperty("has_dyslexia", profile.hasDyslexia)
                    addProperty("has_autism", profile.hasAutism)
                    addProperty("has_anxiety", profile.hasAnxiety)
                })
            }

            val response = api.askQuestion(requestBody)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!)
            } else {
                ApiResult.Error(errorMessage(response.code()))
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }

    private fun resolveSubjectName(
        rawAnswers: Map<String, String>,
        userCategory: String
    ): String {
        return when (userCategory) {
            "uni_student"      -> rawAnswers["uni_subjects"]
                ?.split(",")?.firstOrNull()?.trim()
                ?: "My Subject"
            "language_learner" -> rawAnswers["lang_language"] ?: "Language Study"
            "cert_candidate"   -> rawAnswers["cert_name"] ?: "Certification Prep"
            "self_study"       -> rawAnswers["self_topic"] ?: "Self Study"
            else               -> rawAnswers["uni_subjects"]
                ?.split(",")?.firstOrNull()?.trim()
                ?: "My Subject"
        }
    }

    private fun errorMessage(code: Int) = when (code) {
        429  -> "AI is busy right now. Try again in a minute."
        500  -> "Something went wrong on the server. Please try again."
        else -> "Unexpected error ($code). Please try again."
    }
}