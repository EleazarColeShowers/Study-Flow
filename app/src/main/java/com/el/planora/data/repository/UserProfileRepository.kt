package com.el.planora.data.repository

import com.el.planora.data.mapper.UserProfileMapper
import com.el.planora.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    private fun usersCollection() = firestore.collection("users")

    // ── Save profile after onboarding completes ────────────────────────────────
    suspend fun saveOnboardingProfile(answers: Map<String, Any>): ApiResult<UserProfile> {
        return try {
            val user = auth.currentUser
                ?: return ApiResult.Error("Not logged in")

            val profile = UserProfileMapper.fromOnboardingAnswers(
                uid      = user.uid,
                userName = user.displayName ?: user.email?.substringBefore("@") ?: "Student",
                email    = user.email ?: "",
                answers  = answers
            )

            // Merge so we don't overwrite existing fields (e.g. username set during signup)
            usersCollection()
                .document(user.uid)
                .set(profileToFirestoreMap(profile), SetOptions.merge())
                .await()

            ApiResult.Success(profile)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to save profile")
        }
    }

    // ── Fetch profile (used by API calls) ──────────────────────────────────────
    suspend fun getCurrentUserProfile(): ApiResult<UserProfile> {
        return try {
            val user = auth.currentUser
                ?: return ApiResult.Error("Not logged in")

            val doc = usersCollection()
                .document(user.uid)
                .get()
                .await()

            if (!doc.exists()) return ApiResult.Error("Profile not found")

            val profile = firestoreMapToProfile(user.uid, doc.data ?: emptyMap())
            ApiResult.Success(profile)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to load profile")
        }
    }

    // ── Firestore serialisation ────────────────────────────────────────────────
    private fun profileToFirestoreMap(profile: UserProfile): Map<String, Any> = mapOf(
        "uid"           to profile.uid,
        "userName"      to profile.userName,
        "email"         to profile.email,
        "userCategory"  to profile.userCategory,
        "hasAdhd"       to profile.hasAdhd,
        "hasDyslexia"   to profile.hasDyslexia,
        "hasAutism"     to profile.hasAutism,
        "hasAnxiety"    to profile.hasAnxiety,
        "attentionSpan" to profile.attentionSpan,
        "sleepHours"    to profile.sleepHours,
        "dailyStudyHrs" to profile.dailyStudyHrs,
        "learningStyle" to profile.learningStyle,
        "peakFocusTime" to profile.peakFocusTime,
        "studyEnv"      to profile.studyEnv,
        "struggle"      to profile.struggle,
        "currentLevel"  to profile.currentLevel,
        "priorAttempt"  to profile.priorAttempt,
        "successGoal"   to profile.successGoal,
        "rawAnswers"    to profile.rawAnswers,
        "onboardingComplete" to true,
        "updatedAt"     to com.google.firebase.Timestamp.now()
    )

    private fun firestoreMapToProfile(uid: String, data: Map<String, Any>): UserProfile {
        fun str(key: String) = data[key] as? String ?: ""
        fun int(key: String) = (data[key] as? Long)?.toInt() ?: 0
        fun float(key: String) = (data[key] as? Double)?.toFloat()
            ?: (data[key] as? Long)?.toFloat() ?: 0f

        @Suppress("UNCHECKED_CAST")
        val rawAnswers = (data["rawAnswers"] as? Map<String, String>) ?: emptyMap()

        return UserProfile(
            uid           = uid,
            userName      = str("userName"),
            email         = str("email"),
            userCategory  = str("userCategory").ifEmpty { "uni_student" },
            hasAdhd       = int("hasAdhd"),
            hasDyslexia   = int("hasDyslexia"),
            hasAutism     = int("hasAutism"),
            hasAnxiety    = int("hasAnxiety"),
            attentionSpan = str("attentionSpan").ifEmpty { "20_45" },
            sleepHours    = float("sleepHours").takeIf { it > 0 } ?: 7.0f,
            dailyStudyHrs = float("dailyStudyHrs").takeIf { it > 0 } ?: 2.0f,
            learningStyle = str("learningStyle").ifEmpty { "read_write" },
            peakFocusTime = str("peakFocusTime").ifEmpty { "morning" },
            studyEnv      = str("studyEnv").ifEmpty { "varies" },
            struggle      = str("struggle").ifEmpty { "understanding" },
            currentLevel  = str("currentLevel").ifEmpty { "not_applicable" },
            priorAttempt  = str("priorAttempt").ifEmpty { "not_applicable" },
            successGoal   = str("successGoal"),
            rawAnswers    = rawAnswers
        )
    }
}