package com.el.planora.data.repository

import com.el.planora.data.remote.PlonoraApiService
import com.el.planora.domain.model.DashboardStats
import com.el.planora.domain.model.StudyTrack
import com.el.planora.domain.model.Topic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectsRepository @Inject constructor(
    private val api: PlonoraApiService,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // ── Step 1: Get the Planora user_id from Firestore ─────────────────────────
    // Falls back to Firebase uid if planoraUserId field isn't stored yet
    private suspend fun getPlanoraUserId(): String? {
        val firebaseUid = auth.currentUser?.uid ?: return null
        return try {
            val doc = firestore.collection("users")
                .document(firebaseUid)
                .get()
                .await()
            // Try planoraUserId field first, fall back to Firebase uid
            doc.getString("planoraUserId") ?: firebaseUid
        } catch (e: Exception) {
            firebaseUid // Fall back to Firebase uid
        }
    }

    // ── Step 2: Fetch subjects from Planora API ────────────────────────────────
    suspend fun getSubjects(): ApiResult<List<StudyTrack>> {
        return try {
            val userId = getPlanoraUserId()
                ?: return ApiResult.Error("Not logged in")

            val response = api.getSubjects(userId)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val subjectsArray = body.getAsJsonArray("subjects")
                    ?: return ApiResult.Success(emptyList())

                val tracks = subjectsArray.mapNotNull { element ->
                    element.asJsonObject.toStudyTrack()
                }
                ApiResult.Success(tracks)
            } else if (response.code() == 404) {
                // No subjects yet — not an error
                ApiResult.Success(emptyList())
            } else {
                ApiResult.Error("Failed to load subjects (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }

    // ── Step 3: Calculate stats from subjects ──────────────────────────────────
    fun calculateStats(tracks: List<StudyTrack>): DashboardStats {
        val activePlans = tracks.size
        val averageProgress = if (tracks.isEmpty()) 0
        else tracks.map { it.completionPercentage }.average().toInt()

        return DashboardStats(
            dayStreak      = 0,      // Not available from API yet
            activePlans    = activePlans,
            averageProgress = averageProgress
        )
    }
}

// ── Map API subject JSON → StudyTrack domain model ─────────────────────────────
private fun JsonObject.toStudyTrack(): StudyTrack? {
    return try {
        StudyTrack(
            id                   = get("subject_id")?.asString ?: return null,
            title                = get("subject_name")?.asString ?: "Unknown Subject",
            subject              = get("subject_name")?.asString ?: "",
            exam                 = mapContentType(get("content_type")?.asString),
            coverImageUrl        = "",
            estimatedHours       = get("days_to_exam")?.asInt?.let { it / 3 } ?: 10,
            completionPercentage = get("completion_percentage")?.asInt ?: 0,
            lastActiveDate       = System.currentTimeMillis(),
            topics               = emptyList()
        )
    } catch (e: Exception) {
        null
    }
}

private fun mapContentType(contentType: String?): String {
    return when (contentType) {
        "theory"      -> "Theory & Concepts"
        "calculation" -> "Problem Solving"
        "mixed"       -> "Theory + Practice"
        "practical"   -> "Practical Skills"
        else          -> "General Study"
    }
}