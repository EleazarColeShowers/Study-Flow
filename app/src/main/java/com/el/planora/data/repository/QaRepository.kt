package com.el.planora.data.repository

import com.el.planora.data.remote.PlonoraApiService
import com.el.planora.data.remote.model.QaResponse
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QaRepository @Inject constructor(
    private val api: PlonoraApiService,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) {

    suspend fun askQuestion(question: String): ApiResult<QaResponse> {
        return try {
            val userId = auth.currentUser?.uid
                ?: return ApiResult.Error("Not logged in")

            // Fetch real profile for subject + category
            val profile = when (val result = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> result.data
                is ApiResult.Error   -> return ApiResult.Error(result.message)
                else                 -> return ApiResult.Error("Could not load profile")
            }

            val subjectName = resolveSubjectName(profile.rawAnswers, profile.userCategory)

            val response = api.askQuestion(
                userId       = userId,
                question     = question,
                subjectName  = subjectName,
                userCategory = profile.userCategory
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

    /**
     * Picks the most relevant subject name from raw onboarding answers
     * based on the user's category.
     */
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