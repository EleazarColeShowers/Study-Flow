package com.el.planora.data.repository

import com.el.planora.data.remote.PlonoraApiService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.JsonObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddSubjectRepository @Inject constructor(
    private val api: PlonoraApiService,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    suspend fun addSubject(
        subjectName: String,
        contentType: String,
        memoryLoad: String,
        difficulty: Int,
        hasDeadline: Boolean,
        daysToExam: Int
    ): ApiResult<String> {
        return try {
            val firebaseUid = auth.currentUser?.uid
                ?: return ApiResult.Error("Not logged in")

            // Get the Planora UUID from Firestore
            val doc = firestore.collection("users")
                .document(firebaseUid)
                .get()
                .await()
            val planoraUserId = doc.getString("planoraUserId")
                ?: return ApiResult.Error("User not registered with Planora yet")

            val body = JsonObject().apply {
                addProperty("user_id",      planoraUserId)
                addProperty("subject_name", subjectName.trim())
                addProperty("content_type", contentType)
                addProperty("memory_load",  memoryLoad)
                addProperty("difficulty",   difficulty)
                addProperty("has_deadline", if (hasDeadline) 1 else 0)
                addProperty("days_to_exam", if (hasDeadline) daysToExam else 999)
            }

            val response = api.addSubject(body)

            if (response.isSuccessful && response.body() != null) {
                val subjectId = response.body()!!
                    .get("subject_id")?.asString
                    ?: return ApiResult.Error("No subject_id returned")
                ApiResult.Success(subjectId)
            } else {
                ApiResult.Error("Failed to add subject (${response.code()})")
            }
        } catch (e: Exception) {
            ApiResult.Error("Check your connection and try again")
        }
    }
}