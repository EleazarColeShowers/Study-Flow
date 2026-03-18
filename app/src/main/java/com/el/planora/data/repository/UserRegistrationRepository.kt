package com.el.planora.data.repository

import com.el.planora.data.remote.PlonoraApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRegistrationRepository @Inject constructor(
    private val api: PlonoraApiService,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userProfileRepository: UserProfileRepository
) {
    suspend fun registerWithPlanora(): ApiResult<String> {
        return try {
            val firebaseUid = auth.currentUser?.uid
                ?: return ApiResult.Error("Not logged in")

            // Check if already registered
            val doc = firestore.collection("users")
                .document(firebaseUid).get().await()
            val existingPlanoraId = doc.getString("planoraUserId")
            if (!existingPlanoraId.isNullOrBlank()) {
                return ApiResult.Success(existingPlanoraId) // Already done
            }

            // Fetch profile to build the request
            val profile = when (val r = userProfileRepository.getCurrentUserProfile()) {
                is ApiResult.Success -> r.data
                is ApiResult.Error   -> return ApiResult.Error(r.message)
                else -> return ApiResult.Error("Could not load profile")
            }

            val body = JsonObject().apply {
                addProperty("user_name",      profile.userName)
                addProperty("user_category",  profile.userCategory)
                addProperty("has_adhd",       profile.hasAdhd)
                addProperty("has_dyslexia",   profile.hasDyslexia)
                addProperty("has_autism",     profile.hasAutism)
                addProperty("has_anxiety",    profile.hasAnxiety)
                addProperty("attention_span", profile.attentionSpan)
                addProperty("sleep_hours",    profile.sleepHours)
                addProperty("daily_study_hrs",profile.dailyStudyHrs)
                addProperty("learning_style", profile.learningStyle)
                addProperty("peak_focus_time",profile.peakFocusTime)
                addProperty("study_env",      profile.studyEnv)
                addProperty("struggle",       profile.struggle)
                addProperty("success_goal",   profile.successGoal)
            }

            val response = api.createUser(body)
            if (response.isSuccessful && response.body() != null) {
                val planoraUserId = response.body()!!
                    .get("user_id")?.asString
                    ?: return ApiResult.Error("No user_id in response")

                // Save planoraUserId to Firestore for future use
                firestore.collection("users")
                    .document(firebaseUid)
                    .update("planoraUserId", planoraUserId)
                    .await()

                ApiResult.Success(planoraUserId)
            } else {
                ApiResult.Error("Registration failed (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }
}