package com.el.planora.data.remote

import com.el.planora.data.remote.model.CheckInMessageRequest
import com.el.planora.data.remote.model.CheckInMessageResponse
import com.el.planora.data.remote.model.CheckInStartRequest
import com.el.planora.data.remote.model.CheckInStartResponse
import com.el.planora.data.remote.model.PingResponse
import com.el.planora.data.remote.model.QaResponse
import com.el.planora.data.remote.model.RecommendRequest
import com.el.planora.data.remote.model.RecommendResponse
import com.el.planora.data.repository.CheckInStartRequestFlexible
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PlonoraApiService {

    @GET("ping")
    suspend fun ping(): Response<PingResponse>

    // ── Recommendations ────────────────────────────────────────────────────────
    @POST("recommend")
    suspend fun getRecommendations(
        @Body request: RecommendRequest
    ): Response<RecommendResponse>

    // ── Check-in ───────────────────────────────────────────────────────────────
    @POST("checkin/start")
    suspend fun startCheckIn(
        @Body request: CheckInStartRequest
    ): Response<CheckInStartResponse>

    @POST("checkin/start")
    suspend fun startCheckInFlexible(
        @Body request: CheckInStartRequestFlexible
    ): Response<CheckInStartResponse>

    @POST("checkin/message")
    suspend fun sendMessage(
        @Body request: CheckInMessageRequest
    ): Response<CheckInMessageResponse>

    @DELETE("checkin/{user_id}")
    suspend fun endCheckIn(
        @Path("user_id") userId: String
    ): Response<Unit>

    // ── Study Q&A ──────────────────────────────────────────────────────────────
    // API uses query parameters, not a request body
    @POST("qa")
    suspend fun askQuestion(
        @Query("user_id")      userId: String,
        @Query("question")     question: String,
        @Query("subject_name") subjectName: String,
        @Query("user_category") userCategory: String,
        @Query("context")      context: String? = null
    ): Response<QaResponse>
}